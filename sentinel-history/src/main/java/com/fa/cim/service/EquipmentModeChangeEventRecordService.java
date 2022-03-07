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
 * @date 2019/6/28 14:58
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class EquipmentModeChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EquipmentModeChangeHistoryService equipmentModeChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Transactional(rollbackFor = Exception.class)
    public Response createEquipmentModeChangeEventRecord( Infos.EquipmentModeChangeEventRecord equipmentModeChangeEventRecord, List<Infos.UserDataSet> userDataSets )
    {
        log.info("HistoryWatchDogServer::CreateEquipmentModeChangeEventRecord Function");
        Infos.Ohemchs fhemchs= new Infos.Ohemchs();
        Params.String           eqpName = new Params.String();
        Params.String           eqpArea = new Params.String();
        Params.String           stkName = new Params.String();
        Params.String           stkArea = new Params.String();
        Params.Param<Double>         shopData = new Params.Param<Double>();
        Params.String           workTimeStamp = new Params.String();
        Response iRc = returnOK();
        iRc = tableMethod.getFREQP( equipmentModeChangeEventRecord.getEquipmentID(), eqpArea, eqpName );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateEquipmentModeChangeEventRecordFunction");
            return( iRc );
        }


        fhemchs = new Infos.Ohemchs();
        fhemchs.setEqp_id(equipmentModeChangeEventRecord.getEquipmentID() );
        fhemchs.setEqp_name(eqpName.getValue() );
        fhemchs.setPort_id(equipmentModeChangeEventRecord.getPortID() );
        fhemchs.setArea_id(eqpArea.getValue() );
        fhemchs.setOpe_mode(equipmentModeChangeEventRecord.getOperationMode() );
        fhemchs.setOnline_mode(equipmentModeChangeEventRecord.getOnlineMode() );
        fhemchs.setDisp_mode(equipmentModeChangeEventRecord.getDispatchMode() );
        fhemchs.setAccess_mode(equipmentModeChangeEventRecord.getAccessMode() );
        fhemchs.setOpe_start_mode(equipmentModeChangeEventRecord.getOperationStartMode() );
        fhemchs.setOpe_comp_mode(equipmentModeChangeEventRecord.getOperationCompMode() );
        fhemchs.setDescription(equipmentModeChangeEventRecord.getDescription() );
        fhemchs.setClaim_time(equipmentModeChangeEventRecord.getEventCommon().getEventTimeStamp() );
        fhemchs.setClaim_shop_date(equipmentModeChangeEventRecord.getEventCommon().getEventShopDate() );
        fhemchs.setClaim_user_id(equipmentModeChangeEventRecord.getEventCommon().getUserID() );
        fhemchs.setClaim_memo(equipmentModeChangeEventRecord.getEventCommon().getEventMemo() );
        fhemchs.setEvent_create_time(equipmentModeChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = equipmentModeChangeHistoryService.insertEquipmentModeChangeHistory( fhemchs );
        if( !isOk(iRc) )
        {

            log.info("HistoryWatchDogServer::CreateEquipmentModeChangeEventRecord(): InsertEquipmentModeChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateEquipmentModeChangeEventRecordFunction");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateEquipmentModeChangeEventRecordFunction");
        return ( returnOK() );
    }

}
