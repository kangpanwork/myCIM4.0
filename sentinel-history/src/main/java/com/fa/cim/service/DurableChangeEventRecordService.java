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
import static com.fa.cim.utils.StringUtils.variableStrCmp;
import static com.fa.cim.Constant.SPConstant.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/7/9 15:56
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableChangeHistoryService durableChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param durableChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/9 15:59
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createDurableChangeEventRecord( Infos.DurableChangeEventRecord durableChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDurableChangeEventRecord Function");
        Infos.Ohdrchs        fhdrchs= new Infos.Ohdrchs();
        Response iRc = returnOK();
        fhdrchs = new Infos.Ohdrchs();
        fhdrchs.setDurable_id(durableChangeEventRecord.getDurableID() );
        fhdrchs.setDurable_type(durableChangeEventRecord.getDurableType() );
        fhdrchs.setAction_code(durableChangeEventRecord.getAction() );
        fhdrchs.setDurable_status(durableChangeEventRecord.getDurableStatus() );
        fhdrchs.setXfer_status(durableChangeEventRecord.getXferStatus() );
        fhdrchs.setDurable_sub_status(durableChangeEventRecord.getDurableSubStatus() );
        if( variableStrCmp( durableChangeEventRecord.getAction(),DURABLE_STATUS_CHANGE) == 0 ) {
            fhdrchs.setXfer_stat_chg_time("1901-01-01-00.00.00.000000") ;
        }
        else if(variableStrCmp( durableChangeEventRecord.getAction(),PREVENTIVE_MAINTENANCE_RESET) == 0 ) {
            fhdrchs.setXfer_stat_chg_time("1901-01-01-00.00.00.000000") ;
        }
        else
        {
            fhdrchs.setXfer_stat_chg_time(durableChangeEventRecord.getXferStatChgTimeStamp() );
        }
        fhdrchs.setLocation(durableChangeEventRecord.getLocation() );
        fhdrchs.setClaim_time(durableChangeEventRecord.getEventCommon().getEventTimeStamp() );
        fhdrchs.setClaim_shop_date(durableChangeEventRecord.getEventCommon().getEventShopDate() );
        fhdrchs.setClaim_user_id(durableChangeEventRecord.getEventCommon().getUserID() );
        fhdrchs.setClaim_memo(durableChangeEventRecord.getEventCommon().getEventMemo() );
        fhdrchs.setEvent_create_time(durableChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = durableChangeHistoryService.insertDurableChangeHistory( fhdrchs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableChangeEventRecord(): InsertDurableChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateDurableChangeEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateDurableChangeEventRecord Function");
        return(returnOK());
    }

}
