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
 * @date 2019/6/30 15:06
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class EquipmentFlowBatchMaxCountChangeEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private EquipmentFlowBatchMaxCountChangeHistoryService equipmentFlowBatchMaxCountChangeHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentFlowBatchMaxCountChangeEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/1 16:41
     */
    @Transactional(rollbackFor = Exception.class)
    public Response createEquipmentFlowBatchMaxCountChangeEventRecord( Infos.EquipmentFlowBatchMaxCountChangeEventRecord  equipmentFlowBatchMaxCountChangeEventRecord, List<Infos.UserDataSet>  userDataSets ) {
        log.info("HistoryWatchDogServer::CreateEquipmentFlowBatchMaxCountChangeEventRecord Function");
        Infos.Ohfbmchs  fhfbmchs= new Infos.Ohfbmchs();
        Response iRc = returnOK();
        fhfbmchs = new Infos.Ohfbmchs();
        fhfbmchs.setEqp_id(equipmentFlowBatchMaxCountChangeEventRecord.getEquipmentID()                        );
        fhfbmchs.setMaxCount(equipmentFlowBatchMaxCountChangeEventRecord.getNewFlowBatchMaxCount()==null?
                null:equipmentFlowBatchMaxCountChangeEventRecord.getNewFlowBatchMaxCount().intValue());
        fhfbmchs.setClaimTime(equipmentFlowBatchMaxCountChangeEventRecord.getEventCommon().getEventTimeStamp()         );
        fhfbmchs.setClaimUser(equipmentFlowBatchMaxCountChangeEventRecord.getEventCommon().getUserID()                 );
        fhfbmchs.setClaimMemo(equipmentFlowBatchMaxCountChangeEventRecord.getEventCommon().getEventMemo()              );
        fhfbmchs.setEventCreateTime(equipmentFlowBatchMaxCountChangeEventRecord.getEventCommon().getEventCreationTimeStamp() );
        iRc = equipmentFlowBatchMaxCountChangeHistoryService.insertEquipmentFlowBatchMaxCountChangeHistory( fhfbmchs );
        if( !isOk(iRc) ) {
            log.info("HistoryWatchDogServer::CreateEquipmentFlowBatchMaxCountChangeEventRecord(): InsertEquipmentFlowBatchMaxCountChangeHistory SQL Error Occured");
            log.info("HistoryWatchDogServer::CreateEquipmentFlowBatchMaxCountChangeEventRecord Function");
            return iRc;
        }
        log.info("HistoryWatchDogServer::CreateEquipmentFlowBatchMaxCountChangeEventRecord Function");
        return returnOK();
    }

}
