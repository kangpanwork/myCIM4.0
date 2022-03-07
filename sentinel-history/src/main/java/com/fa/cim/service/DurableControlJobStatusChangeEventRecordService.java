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
import static com.fa.cim.Constant.TransactionConstant.ODRBW030_ID;
import static com.fa.cim.utils.BaseUtils.*;
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
 * @date 2019/7/11 10:37
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class DurableControlJobStatusChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private DurableControlJobStatusChangeHistoryService durableControlJobStatusChangeHistoryService;

    @Autowired
    private DurableOperationStartHistoryService durableOperationStartHistoryService;

    @Transactional(rollbackFor = Exception.class)
    public Response createDurableControlJobStatusChangeEventRecord( Infos.DurableControlJobStatusChangeEventRecord DurableControlJobStatusChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateDurableControlJobStatusChangeEventRecord Function");
        Infos.Ohdcjschs         fhdcjschs_record= new Infos.Ohdcjschs();
        Infos.Ohdcjschs_drbl    fhdcjschs_drbl_record= new Infos.Ohdcjschs_drbl();
        Response iRc = returnOK();
        fhdcjschs_record = new Infos.Ohdcjschs();
        fhdcjschs_drbl_record = new Infos.Ohdcjschs_drbl();
        fhdcjschs_record.setDctrljob_id(DurableControlJobStatusChangeEventRecord.getDurableCtrlJobID()           );
        fhdcjschs_record.setDctrljob_state(DurableControlJobStatusChangeEventRecord.getDurableCtrlJobState()        );
        fhdcjschs_record.setDrbl_category(DurableControlJobStatusChangeEventRecord.getDurables().get(0).getDurableCategory());
        fhdcjschs_record.setEqp_id(DurableControlJobStatusChangeEventRecord.getEqpID()                      );
        fhdcjschs_record.setEqp_description(DurableControlJobStatusChangeEventRecord.getEqpDescription()             );
        fhdcjschs_record.setClaim_time(DurableControlJobStatusChangeEventRecord.getEventCommon().getEventTimeStamp() );
        fhdcjschs_record.setClaim_shop_date(DurableControlJobStatusChangeEventRecord.getEventCommon().getEventShopDate());
        fhdcjschs_record.setClaim_user_id(DurableControlJobStatusChangeEventRecord.getEventCommon().getUserID()         );
        fhdcjschs_record.setClaim_memo(DurableControlJobStatusChangeEventRecord.getEventCommon().getEventMemo()      );
        fhdcjschs_record.setEvent_create_time(DurableControlJobStatusChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = durableControlJobStatusChangeHistoryService.insertDRBLCJStatusChangeHistory_FHDCJSCHS( fhdcjschs_record );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateDurableControlJobStatusChangeEventRecord(): InsertDRBLCJStatusChangeHistory_FHDCJSCHS SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateDurableControlJobStatusChangeEventRecord Function");
            return( iRc );
        }
        if (length(DurableControlJobStatusChangeEventRecord.getDurables())>0 ) {
            for(int i = 0; i<length(DurableControlJobStatusChangeEventRecord.getDurables()); i++ ) {
                fhdcjschs_drbl_record = new Infos.Ohdcjschs_drbl();
                fhdcjschs_drbl_record.setDctrljob_id(DurableControlJobStatusChangeEventRecord.getDurableCtrlJobID()                   );
                fhdcjschs_drbl_record.setDurable_id(DurableControlJobStatusChangeEventRecord.getDurables().get(i).getDurableID()              );
                fhdcjschs_drbl_record.setDrbl_category(DurableControlJobStatusChangeEventRecord.getDurables().get(i).getDurableCategory()        );
                fhdcjschs_drbl_record.setProdspec_id(DurableControlJobStatusChangeEventRecord.getDurables().get(i).getProdSpecID()             );
                fhdcjschs_drbl_record.setMainpd_id(DurableControlJobStatusChangeEventRecord.getDurables().get(i).getMainPDID()               );
                fhdcjschs_drbl_record.setOpe_no(DurableControlJobStatusChangeEventRecord.getDurables().get(i).getOpeNo()                  );
                fhdcjschs_drbl_record.setPd_id(DurableControlJobStatusChangeEventRecord.getDurables().get(i).getPdID()                   );
                fhdcjschs_drbl_record.setOpe_pass_count( DurableControlJobStatusChangeEventRecord.getDurables().get(i).getOpePassCount());
                fhdcjschs_drbl_record.setPd_name(DurableControlJobStatusChangeEventRecord.getDurables().get(i).getPdName()                 );
                fhdcjschs_drbl_record.setClaim_time(DurableControlJobStatusChangeEventRecord.getEventCommon().getEventTimeStamp()         );
                iRc = durableControlJobStatusChangeHistoryService.insertDRBLCJStatusChangeHistory_FHDCJSCHS_DRBL( fhdcjschs_drbl_record );
                if( !isOk(iRc) ) {
                    log.info("HistoryWatchDogServer::CreateDurableControlJobStatusChangeEventRecord(): InsertDRBLCJStatusChangeHistory_FHDCJSCHS_DRBL SQL Error Occured");
                    log.info("HistoryWatchDogServer::CreateDurableControlJobStatusChangeEventRecord Function");
                    return( iRc );
                }
            }
        }
        log.info("HistoryWatchDogServer::CreateDurableControlJobStatusChangeEventRecord Function");
        return( returnOK() );
    }

}
