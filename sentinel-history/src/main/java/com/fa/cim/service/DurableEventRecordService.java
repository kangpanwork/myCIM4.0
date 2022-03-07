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
 * @date 2019/7/11 10:37
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableHistoryService durableHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param durableEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/11 11:19
     */
    @Transactional(rollbackFor = Exception.class)
    public Response  createDurableEventRecord( Infos.DurableEventRecord  durableEventRecord, List<Infos.UserDataSet>  userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDurableEventRecord Function");
        Infos.Ohdrblhs  fhdrblhs= new Infos.Ohdrblhs();
        Response iRc = returnOK();
        fhdrblhs = new Infos.Ohdrblhs();
        fhdrblhs.setAction(durableEventRecord.getAction()                             );
        fhdrblhs.setDurable_type(durableEventRecord.getDurableType()                        );
        fhdrblhs.setDurable_id(durableEventRecord.getDurableID()                          );
        fhdrblhs.setDescription(durableEventRecord.getDescription()                        );
        fhdrblhs.setCategory_id(durableEventRecord.getCategoryID()                         );
        fhdrblhs.setInstance_name(durableEventRecord.getInstanceName()                       );
        fhdrblhs.setUsage_check_required(durableEventRecord.getUsageCheckRequiredFlag());
        fhdrblhs.setDuration_limit(durableEventRecord.getDurationLimit());
        fhdrblhs.setTimes_used_limit(durableEventRecord.getTimeUsedLimit()==null?
                null:durableEventRecord.getTimeUsedLimit().intValue());
        fhdrblhs.setInterval_between_pm(durableEventRecord.getIntervalBetweenPM()==null?
                null:durableEventRecord.getIntervalBetweenPM().intValue());
        fhdrblhs.setContents(durableEventRecord.getContents()                           );
        fhdrblhs.setContents_size(durableEventRecord.getContentsSize()==null?
                null:durableEventRecord.getContentsSize().intValue());
        fhdrblhs.setCapacity(durableEventRecord.getCapacity()==null?
                null:durableEventRecord.getCapacity().intValue());
        fhdrblhs.setClaim_time(durableEventRecord.getEventCommon().getEventTimeStamp()         );
        fhdrblhs.setClaim_shop_date(durableEventRecord.getEventCommon().getEventShopDate());
        fhdrblhs.setClaim_user_id(durableEventRecord.getEventCommon().getUserID()                 );
        fhdrblhs.setClaim_memo(durableEventRecord.getEventCommon().getEventMemo()              );
        fhdrblhs.setEvent_create_time(durableEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = durableHistoryService.insertDurableHistory( fhdrblhs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableEventRecord(): InsertDurableHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateDurableEventRecord Function");
            return iRc;
        }
        log.info("HistoryWatchDogServer::CreateDurableEventRecord Function");
        return returnOK();
    }

}
