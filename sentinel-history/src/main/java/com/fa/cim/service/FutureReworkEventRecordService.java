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
 * @date 2019/7/4 13:30
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class FutureReworkEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private FutureReworkHistoryService futureReworkHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param futureReworkEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 13:55
     */
    //@Transactional(rollbackFor = Exception.class)
    public Response createFutureReworkEventRecord( Infos.FutureReworkEventRecord futureReworkEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateFutureReworkEventRecord Function");
        Infos.Ohfrwkhs       fhfrwkhs= new Infos.Ohfrwkhs();
        Response iRc = returnOK();
        Params.String                  codeDescription = new Params.String();
        for( int i = 0; i < length(futureReworkEventRecord.getReworkRoutes()); i++ ) {
            iRc = tableMethod.getFRCODE( futureReworkEventRecord.getReworkRoutes().get(i).getReasonCodeID(), codeDescription );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateFutureReworkEventRecord Function");
                return( iRc );
            }
            fhfrwkhs = new Infos.Ohfrwkhs();
            fhfrwkhs.setAction_code(futureReworkEventRecord.getAction()                                  );
            fhfrwkhs.setLot_id(futureReworkEventRecord.getLotID()                                   );
            fhfrwkhs.setMainpd_id(futureReworkEventRecord.getRouteID()                                 );
            fhfrwkhs.setOpe_no(futureReworkEventRecord.getOperationNumber()                         );
            fhfrwkhs.setTrigger(futureReworkEventRecord.getReworkRoutes().get(i).getTrigger()                 );
            fhfrwkhs.setRwk_route_id(futureReworkEventRecord.getReworkRoutes().get(i).getReworkRouteID()           );
            fhfrwkhs.setReturn_ope_no(futureReworkEventRecord.getReworkRoutes().get(i).getReturnOperationNumber()   );
            fhfrwkhs.setReason_code(futureReworkEventRecord.getReworkRoutes().get(i).getReasonCodeID().getIdentifier() );
            fhfrwkhs.setReason_description(codeDescription.getValue()                                                  );
            fhfrwkhs.setClaim_time(futureReworkEventRecord.getEventCommon().getEventTimeStamp()              );
            fhfrwkhs.setClaim_shop_date(futureReworkEventRecord.getEventCommon().getEventShopDate());
            fhfrwkhs.setClaim_user_id(futureReworkEventRecord.getEventCommon().getUserID()                      );
            fhfrwkhs.setClaim_memo(futureReworkEventRecord.getEventCommon().getEventMemo()                   );
            fhfrwkhs.setEvent_create_time(futureReworkEventRecord.getEventCommon().getEventCreationTimeStamp()      );
            iRc = futureReworkHistoryService.insertFutureReworkHistory( fhfrwkhs );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::CreateFutureReworkEventRecord(): InsertFutureReworkHistory SQL Error Occured");
                log.info("HistoryWatchDogServer::CreateFutureReworkEventRecord Function");
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::CreateFutureReworkEventRecord Function");
        return( returnOK() );
    }

}
