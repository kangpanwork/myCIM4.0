package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @exception
 * @date 2019/6/6 16:48
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class ProcessResourceWaferPositionEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private ProcessResourceWaferPositionHistoryService processResourceWaferPositionHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param processResourceWaferPositionEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @throws
     * @author Ho
     * @date 2019/6/14 10:34
     */
    public Response createProcessResourceWaferPositionEventRecord(Infos.ProcessResourceWaferPositionEventRecord processResourceWaferPositionEventRecord,
                                                                  List<Infos.UserDataSet> userDataSets) {
        Infos.Ohprwphs fhprwphs = new Infos.Ohprwphs();
        Params.String areaID = new Params.String();
        Params.String eqpName = new Params.String();
        Timestamp shopData = new Timestamp(0);
        Response iRc = returnOK();


        iRc = tableMethod.getFREQP(processResourceWaferPositionEventRecord.getEquipmentID(), areaID, eqpName);
        if (!isOk(iRc)) {
            return (iRc);
        }

        iRc = tableMethod.getFRCALENDAR(processResourceWaferPositionEventRecord.getProcessTime(), shopData);
        if (!isOk(iRc)) {
            return (iRc);
        }

        fhprwphs = new Infos.Ohprwphs();
        fhprwphs.setWafer_id(processResourceWaferPositionEventRecord.getWaferID());
        fhprwphs.setLot_id(processResourceWaferPositionEventRecord.getLotID());
        fhprwphs.setCtrljob_id(processResourceWaferPositionEventRecord.getControlJobID());
        fhprwphs.setMainpd_id(processResourceWaferPositionEventRecord.getMainPDID());
        fhprwphs.setOpe_no(processResourceWaferPositionEventRecord.getOpeNo());
        fhprwphs.setOpe_pass_count(processResourceWaferPositionEventRecord.getOpePassCount() == null ?
                null : processResourceWaferPositionEventRecord.getOpePassCount().intValue());
        fhprwphs.setEqp_id(processResourceWaferPositionEventRecord.getEquipmentID());
        fhprwphs.setEqp_name(eqpName.getValue());
        fhprwphs.setProcrsc_id(processResourceWaferPositionEventRecord.getProcessResourceID());
        fhprwphs.setWafer_position(processResourceWaferPositionEventRecord.getWaferPosition());
        fhprwphs.setProc_time(processResourceWaferPositionEventRecord.getProcessTime());
        fhprwphs.setProc_shop_date(convertD(shopData.getTime()));
        fhprwphs.setClaim_time(processResourceWaferPositionEventRecord.getEventCommon().getEventTimeStamp());
        fhprwphs.setClaim_shop_date(processResourceWaferPositionEventRecord.getEventCommon().getEventShopDate());
        fhprwphs.setClaim_user_id(processResourceWaferPositionEventRecord.getEventCommon().getUserID());
        fhprwphs.setClaim_memo(processResourceWaferPositionEventRecord.getEventCommon().getEventMemo());
        fhprwphs.setEvent_create_time(processResourceWaferPositionEventRecord.getEventCommon().getEventCreationTimeStamp());

        iRc = processResourceWaferPositionHistoryService.insertProcessResourceWaferPositionHistory(fhprwphs);
        if (!isOk(iRc)) {
            return (iRc);
        }

        return (returnOK());
    }

}
