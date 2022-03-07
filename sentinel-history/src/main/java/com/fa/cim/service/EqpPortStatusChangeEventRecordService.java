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
 * @date 2019/6/28 14:06
 */
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class EqpPortStatusChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EqpPortStatusChangeHistoryService eqpPortStatusChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    @Transactional(rollbackFor = Exception.class)
    public Response createEqpPortStatusChangeEventRecord( Infos.EqpPortStatusChangeEventRecord eqpPortStatusChangeEventRecord, List<Infos.UserDataSet> userDataSets )
    {
        log.info("HistoryWatchDogServer::CreateDynamicBufferResourceChangeEventRecord Function");
        Infos.Oheqpportschs fheqpportschs= new Infos.Oheqpportschs();
        Response iRc = returnOK();
        fheqpportschs.setPort_type(eqpPortStatusChangeEventRecord.getPortType()                           );
        fheqpportschs.setPort_id(eqpPortStatusChangeEventRecord.getPortID()                             );
        fheqpportschs.setEqp_id(eqpPortStatusChangeEventRecord.getEquipmentID()                        );
        fheqpportschs.setPort_usage(eqpPortStatusChangeEventRecord.getPortUsage()                          );
        fheqpportschs.setPort_state(eqpPortStatusChangeEventRecord.getPortStatus()                         );
        fheqpportschs.setAccess_mode(eqpPortStatusChangeEventRecord.getAccessMode()                         );
        fheqpportschs.setDiap_state(eqpPortStatusChangeEventRecord.getDispatchState()                      );
        fheqpportschs.setDiap_time(eqpPortStatusChangeEventRecord.getDispatchTime()                       );
        fheqpportschs.setDiap_debl_id(eqpPortStatusChangeEventRecord.getDispatchDurableID()                  );
        fheqpportschs.setClaim_time(eqpPortStatusChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        fheqpportschs.setClaim_user_id(eqpPortStatusChangeEventRecord.getEventCommon().getUserID()                 );
        fheqpportschs.setClaim_memo(eqpPortStatusChangeEventRecord.getEventCommon().getEventMemo()              );
        fheqpportschs.setStore_time(""                                                                 );
        fheqpportschs.setEvent_create_time(eqpPortStatusChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = eqpPortStatusChangeHistoryService.insertEqpPortStatusChangeHistory( fheqpportschs );
        if( !isOk(iRc) )
        {
            log.info("HistoryWatchDogServer::CreateEqpPortStatusChangeEventRecord(): InsertEqpPortStatusChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateEqpPortStatusChangeEventRecord Function");
            return( iRc );
        }
        log.info("HistoryWatchDogServer::CreateEqpPortStatusChangeEventRecord Function");
        return ( returnOK() );
    }

}
