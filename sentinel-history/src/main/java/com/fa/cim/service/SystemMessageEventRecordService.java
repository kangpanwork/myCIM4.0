package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import com.fa.cim.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
//@Transactional(rollbackFor = Exception.class)
public class SystemMessageEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private SystemMessageHistoryService systemMessageHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param systemMessageEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 13:58
     */
    public Response createSystemMessageEventRecord(Infos.SystemMessageEventRecord systemMessageEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohsysmhs     fhsysmhs ;
        Infos.Frpd         resultData ;
        Response iRc = returnOK();

        log.info("HistoryWatchDogServer::CreateSystemMessageEventRecord Function" );
        resultData =new Infos.Frpd();
        iRc = tableMethod.getFRPD( systemMessageEventRecord.getOperationID() , resultData );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateSystemMessageEventRecord Function" );
            return( iRc );
        }
        fhsysmhs=new Infos.Ohsysmhs();

        fhsysmhs.setSub_system_id(     systemMessageEventRecord.getSubSystemID() );
        fhsysmhs.setSys_msg_code(      systemMessageEventRecord.getSystemMessageCode() );
        fhsysmhs.setSys_msg_text(systemMessageEventRecord.getSystemMessageText());
//        if(StringUtils.length(systemMessageEventRecord.getSystemMessageText()) >= StringUtils.length(fhsysmhs.getSys_msg_text())) {
//            fhsysmhs.setSys_msg_text(systemMessageEventRecord.getSystemMessageText());
//        }
//        else
//        {
//            fhsysmhs.setSys_msg_text(      systemMessageEventRecord.getSystemMessageText() );
//        }
        fhsysmhs.setNotify_flag      ( convertI(systemMessageEventRecord.getNotifyFlag()) );
        fhsysmhs.setEqp_id(            systemMessageEventRecord.getEquipmentID() );
        fhsysmhs.setEqp_state(         systemMessageEventRecord.getEquipmentState() );
        fhsysmhs.setStk_id(            systemMessageEventRecord.getStockerID() );
        fhsysmhs.setStk_state(         systemMessageEventRecord.getStockerState() );
        fhsysmhs.setAgv_id(            systemMessageEventRecord.getAGVID() );
        fhsysmhs.setAgv_state(         systemMessageEventRecord.getAGVState() );
        fhsysmhs.setLot_id(            systemMessageEventRecord.getLotID() );
        fhsysmhs.setLot_state(         systemMessageEventRecord.getLotState() );
        fhsysmhs.setMainpd_id(         systemMessageEventRecord.getRouteID() );
        fhsysmhs.setOpe_no(            systemMessageEventRecord.getOperationNumber() );
        fhsysmhs.setPd_id(             systemMessageEventRecord.getOperationID() );

        if( resultData.getOperationName()==null ) {
            fhsysmhs.setPd_name(  "" );
        }
        else
        {
            fhsysmhs.setPd_name(           resultData.getOperationName() );
        }

        fhsysmhs.setClam_time(         systemMessageEventRecord.getEventCommon().getEventTimeStamp() );
        fhsysmhs.setClam_shop_date   ( systemMessageEventRecord.getEventCommon().getEventShopDate() );
        fhsysmhs.setClam_user_id(      systemMessageEventRecord.getEventCommon().getUserID() );
        fhsysmhs.setClaim_memo(        systemMessageEventRecord.getEventCommon().getEventMemo() );
        fhsysmhs.setEvent_create_time( systemMessageEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = systemMessageHistoryService.insertSystemMessageHistory( fhsysmhs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::createSystemMessageEventRecord(): InsertSystemMessageHistory SQL Error Occured" );

            log.info("HistoryWatchDogServer::CreateSystemMessageEventRecord Function" );
            return( iRc );
        }

        log.info("HistoryWatchDogServer::CreateSystemMessageEventRecord Function" );
        return(returnOK());
    }

}
