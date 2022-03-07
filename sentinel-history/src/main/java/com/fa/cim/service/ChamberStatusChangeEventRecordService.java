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
public class ChamberStatusChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private ChamberStatusChangeHistoryService chamberStatusChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param chamberStatusChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 17:22
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createChamberStatusChangeEventRecord( Infos.ChamberStatusChangeEventRecord chamberStatusChangeEventRecord,
                                                          List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateChamberStatusChangeEventRecord Function");
        Infos.Ohcschs        fhcschs= new Infos.Ohcschs();
        Params.String                  eqpName = new Params.String();
        Params.String                  areaID = new Params.String();
        Timestamp shopData = new Timestamp(0);
        Params.String                  workTimeStamp = new Params.String();
        Response iRc = returnOK();
        iRc = tableMethod.getFREQP( chamberStatusChangeEventRecord.getEquipmentID(),areaID,eqpName);
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateChamberStatusChangeEventRecord Function");
            return( iRc );
        }
//        if( length(chamberStatusChangeEventRecord.getStartTimeStamp()) < sizeof(fhcschs.getStart_time())-1)
        if( length(chamberStatusChangeEventRecord.getStartTimeStamp()) < 1) {
            iRc = tableMethod.getFRCALENDAR( chamberStatusChangeEventRecord.getEventCommon().getEventTimeStamp(),shopData );
            if( !isOk(iRc) )
            {
                log.info("HistoryWatchDogServer::CreateChamberStatusChangeEventRecord Function");
                return( iRc );
            }
        } else {
            iRc = tableMethod.getFRCALENDAR( chamberStatusChangeEventRecord.getStartTimeStamp(),shopData );
            if( !isOk(iRc) )
            {
                log.info("HistoryWatchDogServer::CreateChamberStatusChangeEventRecord Function");
                return( iRc );
            }
        }
        fhcschs = new Infos.Ohcschs();
        fhcschs.setEqp_id(chamberStatusChangeEventRecord.getEquipmentID() );
        fhcschs.setEqp_name(eqpName.getValue() );
        fhcschs.setProcrsc_id(chamberStatusChangeEventRecord.getProcessResourceID() );
        fhcschs.setArea_id(areaID.getValue() );
        fhcschs.setPr_state(chamberStatusChangeEventRecord.getProcessResourceState() );
        fhcschs.setClaim_user_id(chamberStatusChangeEventRecord.getEventCommon().getUserID() );
//        if( length(chamberStatusChangeEventRecord.getStartTimeStamp()) < sizeof(fhcschs.getStart_time())-1)
        if( length(chamberStatusChangeEventRecord.getStartTimeStamp()) < 1)
        {
            workTimeStamp = new Params.String();
            workTimeStamp .setValue(chamberStatusChangeEventRecord.getEventCommon().getEventTimeStamp());
            fhcschs.setStart_time(workTimeStamp.getValue());
        }
        else
        {
            fhcschs.setStart_time(chamberStatusChangeEventRecord.getStartTimeStamp() ) ;
        }
        fhcschs.setStart_shop_date(convertD(shopData.getTime()));
        workTimeStamp = new Params.String();
        workTimeStamp.setValue(chamberStatusChangeEventRecord.getEventCommon().getEventTimeStamp());
        fhcschs.setEnd_time(workTimeStamp.getValue());
        fhcschs.setEnd_shop_date(chamberStatusChangeEventRecord.getEventCommon().getEventShopDate() );
        fhcschs.setNew_pr_state(chamberStatusChangeEventRecord.getNewProcessResourceState() );
        fhcschs.setClaim_memo(chamberStatusChangeEventRecord.getEventCommon().getEventMemo() );
        fhcschs.setE10_state(chamberStatusChangeEventRecord.getProcessResourceE10State() );
        fhcschs.setAct_e10_state(chamberStatusChangeEventRecord.getActualProcessResourceE10State() );
        fhcschs.setAct_chamber_state(chamberStatusChangeEventRecord.getActualProcessResourceState() );
        fhcschs.setNew_e10_state(chamberStatusChangeEventRecord.getNewProcessResourceE10State() );
        fhcschs.setNew_chamber_state(chamberStatusChangeEventRecord.getNewProcessResourceState() );
        fhcschs.setNew_act_e10_state(chamberStatusChangeEventRecord.getNewActualProcessResourceE10State() );
        fhcschs.setNew_act_chamber_state(chamberStatusChangeEventRecord.getNewActualProcessResourceState() );
        fhcschs.setEvent_create_time(chamberStatusChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = chamberStatusChangeHistoryService.insertChamberStatusChangeHistory( fhcschs );
        if( !isOk(iRc) )
        {

            log.info("HistoryWatchDogServer::CreateChamberStatusChangeEventRecord(): InsertChamberStatusChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateChamberStatusChangeEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateChamberStatusChangeEventRecord Function");
        return(returnOK());
    }

}
