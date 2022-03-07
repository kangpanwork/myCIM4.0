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
 * @exception
 * @author Ho
 * @date 2019/6/6 16:48
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class WaferChamberProcessEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private WaferChamberProcessHistoryService waferChamberProcessHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param waferChamberProcessEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/14 11:35
     */
    public Response createWaferChamberProcessEventRecord(Infos.WaferChamberProcessEventRecord waferChamberProcessEventRecord,
                                                         List<Infos.UserDataSet> userDataSets ) {
        Infos.Ohwcphs fhwcphs=new Infos.Ohwcphs();
        Params.String areaID  =new Params.String();
        Params.String eqpName =new Params.String();
        Timestamp            shopData=new Timestamp(0);
        Response               iRc = returnOK();

        iRc = tableMethod.getFREQP( waferChamberProcessEventRecord.getEquipmentID(), areaID, eqpName );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        iRc = tableMethod.getFRCALENDAR( waferChamberProcessEventRecord.getProcessTime(), shopData );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhwcphs=new Infos.Ohwcphs();
        fhwcphs.setCtrl_job(waferChamberProcessEventRecord.getControlJobID ());  //DSN000015229
        fhwcphs.setWafer_id(waferChamberProcessEventRecord.getWaferID ());
        fhwcphs.setLot_id(waferChamberProcessEventRecord.getLotID ());
        fhwcphs.setMainpd_id(waferChamberProcessEventRecord.getRouteID ());
        fhwcphs.setOpe_no(waferChamberProcessEventRecord.getOpeNo ());
        fhwcphs.setOpe_pass_count  (waferChamberProcessEventRecord.getPassCount()==null?
                null:waferChamberProcessEventRecord.getPassCount().intValue());
        fhwcphs.setEqp_id(waferChamberProcessEventRecord.getEquipmentID ());
        fhwcphs.setEqp_name(eqpName.getValue() );
        fhwcphs.setProcrsc_id(waferChamberProcessEventRecord.getProcessResourceID ());
        fhwcphs.setProc_time(waferChamberProcessEventRecord.getProcessTime ());
        fhwcphs.setProc_shop_date  (convertD(shopData.getTime()));
        fhwcphs.setClaim_time(waferChamberProcessEventRecord.getEventCommon().getEventTimeStamp ());
        fhwcphs.setClaim_shop_date (waferChamberProcessEventRecord.getEventCommon().getEventShopDate());
        fhwcphs.setClaim_user_id(waferChamberProcessEventRecord.getEventCommon().getUserID ());
        fhwcphs.setClaim_memo(waferChamberProcessEventRecord.getEventCommon().getEventMemo ());
        fhwcphs.setAction_code(waferChamberProcessEventRecord.getActionCode ());
        fhwcphs.setEvent_create_time(waferChamberProcessEventRecord.getEventCommon().getEventCreationTimeStamp ());

        iRc = waferChamberProcessHistoryService.insertWaferChamberProcessHistory( fhwcphs );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        return( returnOK() );
    }

}
