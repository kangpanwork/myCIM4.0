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
public class NoteChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private NoteChangeHistoryService noteChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param noteChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/11 10:49
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createNoteChangeEventRecord( Infos.NoteChangeEventRecord noteChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateNoteChangeEventRecord Function");
        Infos.Ohnotehs fhnotehs= new Infos.Ohnotehs();
        fhnotehs = new Infos.Ohnotehs();
        Response iRc = returnOK();
        fhnotehs.setObject_id(noteChangeEventRecord.getObjectID() );
        fhnotehs.setNote_type(noteChangeEventRecord.getNoteType() );
        fhnotehs.setAction(noteChangeEventRecord.getAction() );
        fhnotehs.setMainpd_id(noteChangeEventRecord.getRouteID() );
        fhnotehs.setPd_id(noteChangeEventRecord.getOperationID() );
        fhnotehs.setOpe_no(noteChangeEventRecord.getOperationNumber() );
        fhnotehs.setNote_title(noteChangeEventRecord.getNoteTitle() );
        fhnotehs.setNote_contents(noteChangeEventRecord.getNoteContents() );
        fhnotehs.setOwner_id(noteChangeEventRecord.getOwnerID() );
        fhnotehs.setClaim_user_id(noteChangeEventRecord.getEventCommon().getUserID() );
        fhnotehs.setClaim_memo(noteChangeEventRecord.getEventCommon().getEventMemo() );
        fhnotehs.setClaim_time(noteChangeEventRecord.getEventCommon().getEventTimeStamp() );
        fhnotehs.setClaim_shop_date(noteChangeEventRecord.getEventCommon().getEventShopDate());
        fhnotehs.setEvent_create_time(noteChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = noteChangeHistoryService.insertNoteChangeHistory( fhnotehs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateNoteChangeEventRecord(): InsertNoteChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateNoteChangeEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateNoteChangeEventRecord Function");
        return ( returnOK() );
    }

}
