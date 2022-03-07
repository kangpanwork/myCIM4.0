package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.Constant.SPConstant.*;
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
@Transactional(rollbackFor = Exception.class)
public class ScriptParameterChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private ScriptParameterChangeHistoryService scriptParameterChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param scriptParameterChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/30 13:42
     */
    public Response createScriptParameterChangeEventRecord(Infos.ScriptParameterChangeEventRecord scriptParameterChangeEventRecord, List<Infos.UserDataSet> userDataSets ) {
        log.info("HistoryWatchDogServer::CreateScriptParameterChangeEventRecord Function" );

        Infos.Ohbrpchs       fhbrpchs;
        int                   i;
        Response iRc = returnOK();
        for(i = 0 ; i < length(scriptParameterChangeEventRecord.getUserParameterValues()); i++) {
            fhbrpchs=new Infos.Ohbrpchs();
            fhbrpchs.setParameter_class(     scriptParameterChangeEventRecord.getParameterClass()                       );
            fhbrpchs.setIdentifier(          scriptParameterChangeEventRecord.getIdentifier()                           );

            switch( convertI(scriptParameterChangeEventRecord.getUserParameterValues().get(i).getChangeType()) ) {
                case SP_PARVAL_NOCHANGE:
                    fhbrpchs.setChange_type( SP_PARVAL_CHANGETYPE_NOCHANGE );
                    break;
                case SP_PARVAL_ADD:
                    fhbrpchs.setChange_type( SP_PARVAL_CHANGETYPE_ADD );
                    break;
                case SP_PARVAL_UPDATE:
                    fhbrpchs.setChange_type( SP_PARVAL_CHANGETYPE_UPDATE );
                    break;
                case SP_PARVAL_DELETE:
                    fhbrpchs.setChange_type( SP_PARVAL_CHANGETYPE_DELETE );
                    break;
                default:
                    break;
            }
            fhbrpchs.setParameter_name(      scriptParameterChangeEventRecord.getUserParameterValues().get(i).getParameterName() );
            fhbrpchs.setData_type(           scriptParameterChangeEventRecord.getUserParameterValues().get(i).getDataType()      );
            fhbrpchs.setKey_value(           scriptParameterChangeEventRecord.getUserParameterValues().get(i).getKeyValue()      );
            fhbrpchs.setValue(               scriptParameterChangeEventRecord.getUserParameterValues().get(i).getValue()         );
            fhbrpchs.setValue_flag         ( convertI(scriptParameterChangeEventRecord.getUserParameterValues().get(i).getValueFlag()));
            fhbrpchs.setDescription(         scriptParameterChangeEventRecord.getUserParameterValues().get(i).getDescription()   );

            fhbrpchs.setEvent_time(          scriptParameterChangeEventRecord.getEventCommon().getEventTimeStamp() );
            fhbrpchs.setEvent_shop         ( scriptParameterChangeEventRecord.getEventCommon().getEventShopDate() );
            fhbrpchs.setClaim_user_id(       scriptParameterChangeEventRecord.getEventCommon().getUserID()         );
            fhbrpchs.setEvent_memo(          scriptParameterChangeEventRecord.getEventCommon().getEventMemo()      );
            fhbrpchs.setEvent_create_time(   scriptParameterChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
            iRc = scriptParameterChangeHistoryService.insertScriptParameterChangeHistory( fhbrpchs );
            if( !isOk(iRc) ) {
                log.info("HistoryWatchDogServer::createScriptParameterChangeEventRecord(): InsertScriptParameterChangeHistory SQL Error Occured" );

                log.info("HistoryWatchDogServer::CreateScriptParameterChangeEventRecord Function" );
                return( iRc );
            }
        }

        log.info("HistoryWatchDogServer::CreateScriptParameterChangeEventRecord Function" );
        return(returnOK());

    }

}
