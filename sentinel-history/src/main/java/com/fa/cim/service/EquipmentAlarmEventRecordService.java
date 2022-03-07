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

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.utils.BaseUtils.isOk;
import static com.fa.cim.utils.BaseUtils.returnOK;
import static com.fa.cim.utils.StringUtils.length;
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
 * @date 2019/6/28 16:46
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class EquipmentAlarmEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EquipmentAlarmHistoryService equipmentAlarmHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Transactional(rollbackFor = Exception.class)
    public Response createEquipmentAlarmEventRecord( Infos.EquipmentAlarmEventRecord equipmentAlarmEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohalmhs     fhalmhs= new Infos.Ohalmhs();
        Params.String               eqpName  = new Params.String();
        Params.String               areaID          = new Params.String();
        Params.String               description     = new Params.String();
        Response iRc = returnOK();
        log.info("HistoryWatchDogServer::CreateEquipmentAlarmEventRecord Function");
        fhalmhs = new Infos.Ohalmhs();
        if( length( equipmentAlarmEventRecord.getEquipmentID() ) != 0 )
        {
            iRc = tableMethod.getFREQP( equipmentAlarmEventRecord.getEquipmentID() ,    areaID,eqpName );
            if( !isOk(iRc) )
            {
                log.info("HistoryWatchDogServer::CreateEquipmentAlarmEventRecord Function");
                return( iRc );
            }
            fhalmhs.setEqp_name(eqpName.getValue() );
        }
        fhalmhs.setStk_id(equipmentAlarmEventRecord.getStockerID() );
        if( length(equipmentAlarmEventRecord.getStockerID() ) != 0  )
        {
            iRc = tableMethod.getFRSTK( equipmentAlarmEventRecord.getStockerID() ,      areaID , description );
            if( !isOk(iRc) )
            {
                log.info("HistoryWatchDogServer::CreateEquipmentAlarmEventRecord Function");
                return( iRc );
            }
            fhalmhs.setStk_name(description.getValue() );
        }
        fhalmhs.setEqp_id(equipmentAlarmEventRecord.getEquipmentID() );
        fhalmhs.setAgv_id(equipmentAlarmEventRecord.getAGVID() );
        fhalmhs.setAlarm_category(equipmentAlarmEventRecord.getAlarmCategory() );
        fhalmhs.setAlarm_code(equipmentAlarmEventRecord.getAlarmCode() );
        fhalmhs.setAlarm_id(equipmentAlarmEventRecord.getAlarmID() );
        fhalmhs.setAlarm_text(equipmentAlarmEventRecord.getAlarmText() );
        fhalmhs.setClaim_time(equipmentAlarmEventRecord.getEventCommon().getEventTimeStamp() );
        fhalmhs.setClaim_shop_date(equipmentAlarmEventRecord.getEventCommon().getEventShopDate() );
        fhalmhs.setClaim_user_id(equipmentAlarmEventRecord.getEventCommon().getUserID() );
        fhalmhs.setClaim_memo(equipmentAlarmEventRecord.getEventCommon().getEventMemo() );
        fhalmhs.setEvent_create_time(equipmentAlarmEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = equipmentAlarmHistoryService.insertEquipmentAlarmHistory( fhalmhs );
        if( !isOk(iRc) ) {

            log.info("HistoryWatchDogServer::CreateEquipmentAlarmEventRecord(): InsertEquipmentAlarmHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateEquipmentAlarmEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateEquipmentAlarmEventRecord Function");
        return(returnOK());
    }

}
