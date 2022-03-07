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
import static com.fa.cim.utils.StringUtils.strchr;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/6/30 15:06
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class ProcessHoldEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private ProcessHoldHistoryService processHoldHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param processHoldEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/2 15:29
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createProcessHoldEventRecord( Infos.ProcessHoldEventRecord processHoldEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateProcessHoldEventRecord Function");
        Infos.Ohphlhs        fhphlhs= new Infos.Ohphlhs();
        int                   i;
        Response iRc = returnOK();
        Params.String                  description = new Params.String();
        Params.String                  stageID = new Params.String();
        Params.String                  stageGrpID = new Params.String();
        Params.String                  photoLayer = new Params.String();
        Params.String                  moduleNo = new Params.String();
        fhphlhs = new Infos.Ohphlhs();
        description = new Params.String();
        stageID = new Params.String();
        stageGrpID = new Params.String();
        photoLayer = new Params.String();
        moduleNo = new Params.String();
        moduleNo.setValue(processHoldEventRecord.getOperationNumber());
        strchr( moduleNo, '.');
        iRc = tableMethod.getFRPF2( processHoldEventRecord.getRouteID(), moduleNo, stageID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRPOS2( processHoldEventRecord.getRouteID(), processHoldEventRecord.getOperationNumber(), photoLayer );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRSTAGE( stageID.getValue(), stageGrpID );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessHoldEventRecord Function");
            return( iRc );
        }
        iRc = tableMethod.getFRCODE( (processHoldEventRecord.getReasonCodeID()), description );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessHoldEventRecord Function");
            return( iRc );
        }
        fhphlhs.setMainpd_id(processHoldEventRecord.getRouteID()                    );
        fhphlhs.setOpe_no(processHoldEventRecord.getOperationNumber()            );
        fhphlhs.setPd_id(processHoldEventRecord.getOperationID()                );
        fhphlhs.setOpe_name(processHoldEventRecord.getRouteID()                    );
        fhphlhs.setProd_id(processHoldEventRecord.getProductID()                  );
        fhphlhs.setClaim_time(processHoldEventRecord.getEventCommon().getEventTimeStamp() );
        fhphlhs.setClaim_shop_date(processHoldEventRecord.getEventCommon().getEventShopDate() );
        fhphlhs.setClaim_user_id(processHoldEventRecord.getEventCommon().getUserID()         );
        fhphlhs.setEntry_type(processHoldEventRecord.getEntryType()                  );
        fhphlhs.setHold_type(processHoldEventRecord.getHoldType()                   );
        fhphlhs.setReason_code(processHoldEventRecord.getReasonCodeID().getIdentifier()    );
        fhphlhs.setReason_description(description.getValue());
        fhphlhs.setHold_flag(convert(processHoldEventRecord.getWithExecHoldFlag()) );
        fhphlhs.setStage_id(stageID.getValue());
        fhphlhs.setStagegrp_id(stageGrpID.getValue());
        fhphlhs.setPhoto_layer(photoLayer.getValue());
        fhphlhs.setClaim_memo(processHoldEventRecord.getEventCommon().getEventMemo()      );
        fhphlhs.setEvent_create_time(processHoldEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = processHoldHistoryService.insertProcessHoldHistory( fhphlhs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateProcessHoldEventRecord(): InsertProcessHoldHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateProcessHoldEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateProcessHoldEventRecord Function");
        return(returnOK());
    }

}
