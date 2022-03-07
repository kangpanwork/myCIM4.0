package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
public class CollectedDataChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private CollectedDataChangeHistoryService collectedDataChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param CollectedDataChangeEventRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/25 18:01
     */
    public Response createCollectedDataChangeEventRecord( Infos.CollectedDataChangeEventRecord CollectedDataChangeEventRecord) {
        log.info("HistoryWatchDogServer::CreateCollectedDataChangeEventRecord " );

        Infos.Ohcdchghs    fhcdchghs_Record;
        Response iRc = returnOK();
        int changedDCDataSeqLen = length(CollectedDataChangeEventRecord.getChangedDCDataSeq());
        for (int nCnt = 0; nCnt < changedDCDataSeqLen; nCnt ++) {
            fhcdchghs_Record=new Infos.Ohcdchghs();
            log.info("nCnt               : ", nCnt );
            log.info("lotID              : ", CollectedDataChangeEventRecord.getLotID()                                          );
            log.info("ctrljob_id         : ", CollectedDataChangeEventRecord.getControlJobID()                                   );
            log.info("dcdef_id           : ", CollectedDataChangeEventRecord.getDataCollectionDefinitionID()                     );
            log.info("dcitem_name        : ", CollectedDataChangeEventRecord.getChangedDCDataSeq().get(nCnt).getDataCollectionItemName()  );
            log.info("pre_dcitem_value   : ", CollectedDataChangeEventRecord.getChangedDCDataSeq().get(nCnt).getPreviousDataValue()       );
            log.info("cur_dcitem_value   : ", CollectedDataChangeEventRecord.getChangedDCDataSeq().get(nCnt).getCurrentDataValue()        );
            log.info("claimTime          : ", CollectedDataChangeEventRecord.getEventCommon().getEventTimeStamp()                     );
            log.info("claimUser          : ", CollectedDataChangeEventRecord.getEventCommon().getUserID()                             );
            log.info("claimMemo          : ", CollectedDataChangeEventRecord.getEventCommon().getEventMemo()                          );
            log.info("EventCreateTime    : ", CollectedDataChangeEventRecord.getEventCommon().getEventCreationTimeStamp()             );
            fhcdchghs_Record.setLotID             ( CollectedDataChangeEventRecord.getLotID()                                         );
            fhcdchghs_Record.setCtrljob_id        ( CollectedDataChangeEventRecord.getControlJobID()                                  );
            fhcdchghs_Record.setDcdef_id          ( CollectedDataChangeEventRecord.getDataCollectionDefinitionID()                    );
            fhcdchghs_Record.setDcitem_name       ( CollectedDataChangeEventRecord.getChangedDCDataSeq().get(nCnt).getDataCollectionItemName() );
            fhcdchghs_Record.setPre_dcitem_value  ( CollectedDataChangeEventRecord.getChangedDCDataSeq().get(nCnt).getPreviousDataValue()      );
            fhcdchghs_Record.setCur_dcitem_value  ( CollectedDataChangeEventRecord.getChangedDCDataSeq().get(nCnt).getCurrentDataValue()       );
            fhcdchghs_Record.setClaimTime         ( CollectedDataChangeEventRecord.getEventCommon().getEventTimeStamp()                    );
            fhcdchghs_Record.setClaimUser         ( CollectedDataChangeEventRecord.getEventCommon().getUserID()                            );
            fhcdchghs_Record.setClaimMemo         ( CollectedDataChangeEventRecord.getEventCommon().getEventMemo()                         );
            fhcdchghs_Record.setEventCreateTime   ( CollectedDataChangeEventRecord.getEventCommon().getEventCreationTimeStamp()            );
            iRc = collectedDataChangeHistoryService.insertCollectedDataChangeHistory_FHCDCHGHS( fhcdchghs_Record );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createCollectedDataChangeEventRecord(): InsertCollectedDataChangeHistory_FHCDCHGHS SQL Error Occured" );
                log.info("HistoryWatchDogServer::CreateCollectedDataChangeEventRecord Function" );
                return( iRc );
            }
        }
        log.info("HistoryWatchDogServer::CreateCollectedDataChangeEventRecord Function" );
        return( returnOK() );
    }

}
