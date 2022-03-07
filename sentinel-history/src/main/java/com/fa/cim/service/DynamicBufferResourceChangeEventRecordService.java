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
 * @date 2019/7/10 17:22
 */
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class DynamicBufferResourceChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DynamicBufferResourceChangeHistoryService dynamicBufferResourceChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param dynamicBufferResourceChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 17:22
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createDynamicBufferResourceChangeEventRecord( Infos.DynamicBufferResourceChangeEventRecord dynamicBufferResourceChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDynamicBufferResourceChangeEventRecord Function");
        Infos.Ohbuffrscchs fhbuffrscchs= new Infos.Ohbuffrscchs();
        fhbuffrscchs = new Infos.Ohbuffrscchs();
        Response iRc = returnOK();
        fhbuffrscchs.setEqp_id(dynamicBufferResourceChangeEventRecord.getEquipmentID());
        fhbuffrscchs.setEqp_state(dynamicBufferResourceChangeEventRecord.getEquipmentState());
        fhbuffrscchs.setE10_state(dynamicBufferResourceChangeEventRecord.getE10State());
        fhbuffrscchs.setBuffer_category(dynamicBufferResourceChangeEventRecord.getBufferCategory());
        fhbuffrscchs.setSm_capacity(dynamicBufferResourceChangeEventRecord.getSmCapacity());
        fhbuffrscchs.setDynamic_capacity(dynamicBufferResourceChangeEventRecord.getDynamicCapacity());
        fhbuffrscchs.setClaim_shop_date(dynamicBufferResourceChangeEventRecord.getEventCommon().getEventShopDate());
        fhbuffrscchs.setClaim_time(dynamicBufferResourceChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        fhbuffrscchs.setClaim_user_id(dynamicBufferResourceChangeEventRecord.getEventCommon().getUserID()                 );
        fhbuffrscchs.setClaim_memo(dynamicBufferResourceChangeEventRecord.getEventCommon().getEventMemo()              );
        fhbuffrscchs.setEvent_create_time(dynamicBufferResourceChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = dynamicBufferResourceChangeHistoryService.insertDynamicBufferResourceChangeHistory( fhbuffrscchs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDynamicBufferResourceChangeEventRecord(): InsertDynamicBufferResourceChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateDynamicBufferResourceChangeEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateDynamicBufferResourceChangeEventRecord Function");
        return ( returnOK() );
    }

}
