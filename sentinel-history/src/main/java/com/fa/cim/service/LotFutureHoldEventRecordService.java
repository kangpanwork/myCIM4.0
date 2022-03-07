package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
 * @date 2019/7/4 13:30
 */
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class LotFutureHoldEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private LotFutureHoldHistoryService lotFutureHoldHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotFutureHoldEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 13:30
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createLotFutureHoldEventRecord( Infos.LotFutureHoldEventRecord lotFutureHoldEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateLotFutureHoldEventRecord Function");
        Infos.Ohfhldhs        fhfhldhs= new Infos.Ohfhldhs();
        Response iRc = returnOK();
        fhfhldhs = new Infos.Ohfhldhs();
        fhfhldhs.setLot_id(lotFutureHoldEventRecord.getLotID()                     );
        fhfhldhs.setEntry_type(lotFutureHoldEventRecord.getEntryType()                 );
        fhfhldhs.setHold_Type(lotFutureHoldEventRecord.getHoldType()                  );
        fhfhldhs.setReg_reason_code(lotFutureHoldEventRecord.getRegisterReasonCode()        );
        fhfhldhs.setReg_person_id(lotFutureHoldEventRecord.getRegisterPerson()            );
        fhfhldhs.setMainpd_id(lotFutureHoldEventRecord.getRouteID()                   );
        fhfhldhs.setOpe_no(lotFutureHoldEventRecord.getOpeNo()                     );
        fhfhldhs.setSingle_trig_flag(lotFutureHoldEventRecord.getSingleTriggerFlag ());
        fhfhldhs.setPost_flag(lotFutureHoldEventRecord.getPostFlag()                  );
        fhfhldhs.setRelated_lot_id(lotFutureHoldEventRecord.getRelatedLotID()              );
        fhfhldhs.setRel_reason_code(lotFutureHoldEventRecord.getReleaseReasonCode()         );
        fhfhldhs.setClaim_time(lotFutureHoldEventRecord.getEventCommon().getEventTimeStamp());
        fhfhldhs.setClaim_shop_date(lotFutureHoldEventRecord.getEventCommon().getEventShopDate() );
        fhfhldhs.setClaim_user_id(lotFutureHoldEventRecord.getEventCommon().getUserID()        );
        fhfhldhs.setClaim_memo(lotFutureHoldEventRecord.getEventCommon().getEventMemo()     );
        fhfhldhs.setEvent_create_time(lotFutureHoldEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = lotFutureHoldHistoryService.insertLotFutureHoldHistory( fhfhldhs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateLotFutureHoldEventRecord(): InsertLotFutureHoldHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateLotFutureHoldEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateLotFutureHoldEventRecord Function");
        return(returnOK());
    }

}
