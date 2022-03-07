package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.Constant.TransactionConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
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
 * @date 2019/6/25 13:44
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class EquipmentStatusChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EquipmentStatusChangeHistoryService equipmentStatusChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Transactional(rollbackFor = Exception.class)
    public Response createEquipmentStatusChangeEventRecord( Infos.EquipmentStatusChangeEventRecord eventRecord,
                                                            List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateEquipmentStatusChangeEventRecord Function");
        Infos.Oheschs fheschs= new Infos.Oheschs();
        Params.String eqpName=new Params.String();
        Params.String eqpArea=new Params.String();
        Params.String stkName=new Params.String();
        Params.String stkArea=new Params.String();
        Timestamp shopData=new Timestamp(0);
        Params.String codeDescription = new Params.String();
        Params.String workTimeStamp=new Params.String();
        Response iRc = returnOK();
        iRc = tableMethod.getFRCALENDAR( eventRecord.getStartTimeStamp(),shopData );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateEquipmentStatusChangeEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRCODE(eventRecord.getReasonCode(), codeDescription);
        if (!isOk(iRc)) {
            return iRc;
        }
        fheschs = new Infos.Oheschs();
        fheschs.setReasonCode(eventRecord.getReasonCode().getIdentifier());
        fheschs.setReasonDescription(codeDescription.getValue());
        if(length( eventRecord.getEquipmentID()) != 0 ) {
            iRc = tableMethod.getFREQP( eventRecord.getEquipmentID(),eqpArea,eqpName );
            if( !isOk(iRc) )
            {
                log.info("HistoryWatchDogServer::CreateEquipmentStatusChangeEventRecord Function");
                return( iRc );
            }
            fheschs.setEqp_id(eventRecord.getEquipmentID() );
            fheschs.setEqp_name(eqpName.getValue() );
            fheschs.setArea_id(eqpArea.getValue() );
        }
        if(length(eventRecord.getStockerID() ) != 0 ) {
            iRc = tableMethod.getFRSTK( eventRecord.getStockerID(),stkArea,stkName );
            if( !isOk(iRc) )
            {
                log.info("HistoryWatchDogServer::CreateEquipmentStatusChangeEventRecord Function");
                return( iRc );
            }
            fheschs.setStk_id(eventRecord.getStockerID() );
            fheschs.setStk_name(stkName.getValue());
            fheschs.setArea_id(stkArea .getValue());
        }
        fheschs.setEqp_state(eventRecord.getEquipmentState() );
        fheschs.setClaim_user_id(eventRecord.getEventCommon().getUserID() );
        fheschs.setStart_time(eventRecord.getStartTimeStamp() );
        fheschs.setStart_shop_date(convertD(shopData.getTime()));
        workTimeStamp=new Params.String();
        workTimeStamp.setValue(eventRecord.getEventCommon().getEventTimeStamp());
        fheschs.setEnd_time(workTimeStamp.getValue());
        fheschs.setEnd_shop_date(eventRecord.getEventCommon().getEventShopDate());
        fheschs.setNew_eqp_state(eventRecord.getNewEquipmentState() );
        fheschs.setClaim_memo(eventRecord.getEventCommon().getEventMemo() );
        fheschs.setE10_state(eventRecord.getE10State() );
        fheschs.setAct_e10_state(eventRecord.getActualE10State() );
        fheschs.setAct_equipment_state(eventRecord.getActualEquipmentState() );
        fheschs.setNew_e10_state(eventRecord.getNewE10State() );
        fheschs.setNew_equipment_state(eventRecord.getNewEquipmentState() );
        fheschs.setNew_act_e10_state(eventRecord.getNewActualE10State() );
        fheschs.setNew_act_equipment_state(eventRecord.getNewActualEquipmentState() );
        fheschs.setEvent_create_time(eventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = equipmentStatusChangeHistoryService.insertEquipmentStatusChangeHistory( fheschs );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateEquipmentStatusChangeEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateEquipmentStatusChangeEventRecord Function");
        return ( returnOK() );
    }

}
