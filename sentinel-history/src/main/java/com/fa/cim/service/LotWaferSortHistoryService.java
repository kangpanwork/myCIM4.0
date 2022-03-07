package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
 * @date 2019/5/31 16:16
 */
@Slf4j
@Repository
//@Transactional(rollbackFor = Exception.class)
public class LotWaferSortHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param ChamberStatusChangeRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/28 17:36
     */
    public Response insertChamberStatusChangeHistory(Infos.Ohcschs ChamberStatusChangeRecord ) {
        String hFHCSCHS_EQP_ID                ;
        String hFHCSCHS_EQP_NAME              ;
        String hFHCSCHS_PROCRSC_ID            ;
        String hFHCSCHS_AREA_ID               ;
        String hFHCSCHS_PR_STATE              ;
        String hFHCSCHS_CLAIM_USER_ID         ;
        String hFHCSCHS_START_TIME            ;
        Double  hFHCSCHS_START_SHOP_DATE;
        String hFHCSCHS_END_TIME              ;
        Double  hFHCSCHS_END_SHOP_DATE;
        String hFHCSCHS_NEW_PR_STATE          ;
        String hFHCSCHS_CLAIM_MEMO            ;
        String hFHCSCHS_E10_STATE             ;
        String hFHCSCHS_ACT_E10_STATE         ;
        String hFHCSCHS_ACT_CHAMBER_STATE     ;
        String hFHCSCHS_NEW_E10_STATE         ;
        String hFHCSCHS_NEW_CHAMBER_STATE     ;
        String hFHCSCHS_NEW_ACT_E10_STATE     ;
        String hFHCSCHS_NEW_ACT_CHAMBER_STATE ;
        String hFHCSCHS_EVENT_CREATE_TIME     ;

        hFHCSCHS_EQP_ID = ChamberStatusChangeRecord.getEqp_id          ();
        hFHCSCHS_EQP_NAME = ChamberStatusChangeRecord.getEqp_name        ();
        hFHCSCHS_PROCRSC_ID = ChamberStatusChangeRecord.getProcrsc_id      ();
        hFHCSCHS_AREA_ID = ChamberStatusChangeRecord.getArea_id         ();
        hFHCSCHS_PR_STATE = ChamberStatusChangeRecord.getPr_state        ();
        hFHCSCHS_CLAIM_USER_ID = ChamberStatusChangeRecord.getClaim_user_id   ();
        hFHCSCHS_START_TIME = ChamberStatusChangeRecord.getStart_time      ();
        hFHCSCHS_START_SHOP_DATE  = ChamberStatusChangeRecord.getStart_shop_date ();
        hFHCSCHS_END_TIME = ChamberStatusChangeRecord.getEnd_time        ();
        hFHCSCHS_END_SHOP_DATE    = ChamberStatusChangeRecord.getEnd_shop_date();
        hFHCSCHS_NEW_PR_STATE = ChamberStatusChangeRecord.getNew_pr_state    ();
        hFHCSCHS_CLAIM_MEMO = ChamberStatusChangeRecord.getClaim_memo      ();
        hFHCSCHS_E10_STATE = ChamberStatusChangeRecord.getE10_state             ();
        hFHCSCHS_ACT_E10_STATE = ChamberStatusChangeRecord.getAct_e10_state         ();
        hFHCSCHS_ACT_CHAMBER_STATE = ChamberStatusChangeRecord.getAct_chamber_state     ();
        hFHCSCHS_NEW_E10_STATE = ChamberStatusChangeRecord.getNew_e10_state         ();
        hFHCSCHS_NEW_CHAMBER_STATE = ChamberStatusChangeRecord.getNew_chamber_state     ();
        hFHCSCHS_NEW_ACT_E10_STATE = ChamberStatusChangeRecord.getNew_act_e10_state     ();
        hFHCSCHS_NEW_ACT_CHAMBER_STATE = ChamberStatusChangeRecord.getNew_act_chamber_state ();
        hFHCSCHS_EVENT_CREATE_TIME = ChamberStatusChangeRecord.getEvent_create_time ();


        baseCore.insert("INSERT INTO OHCMBSC\n" +
                "            ( ID, EQP_ID                 ,\n" +
                "                    EQP_NAME               ,\n" +
                "                    PROCRES_ID             ,\n" +
                "                    BAY_ID                ,\n" +
                "                    PROCRES_STATE_ID               ,\n" +
                "                    TRX_USER_ID          ,\n" +
                "                    START_TIME             ,\n" +
                "                    START_WORK_DATE        ,\n" +
                "                    END_TIME               ,\n" +
                "                    END_WORK_DATE          ,\n" +
                "                    NEW_PROCRES_STATE_ID           ,\n" +
                "                    TRX_MEMO             ,\n" +
                "                    STORE_TIME             ,\n" +
                "                    EVENT_CREATE_TIME      ,\n" +
                "                    E10_STATE_ID              ,\n" +
                "                    ACTUAL_E10_STATE_ID          ,\n" +
                "                    ACTUAL_CMB_STATE_ID      ,\n" +
                "                    NEW_E10_STATE_ID          ,\n" +
                "                    NEW_CMB_STATE_ID      ,\n" +
                "                    NEW_ACTUAL_E10_STATE_ID      ,\n" +
                "                    NEW_ACTUAL_CMB_STATE_ID        )\n" +
                "        Values\n" +
                "                (  ?,?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "            CURRENT_TIMESTAMP               ,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?)",generateID(Infos.Ohcschs.class)
                ,hFHCSCHS_EQP_ID
                ,hFHCSCHS_EQP_NAME
                ,hFHCSCHS_PROCRSC_ID
                ,hFHCSCHS_AREA_ID
                ,hFHCSCHS_PR_STATE
                ,hFHCSCHS_CLAIM_USER_ID
                ,convert(hFHCSCHS_START_TIME)
                ,hFHCSCHS_START_SHOP_DATE
                ,hFHCSCHS_END_TIME
                ,hFHCSCHS_END_SHOP_DATE
                ,hFHCSCHS_NEW_PR_STATE
                ,hFHCSCHS_CLAIM_MEMO
                ,convert(hFHCSCHS_EVENT_CREATE_TIME)
                ,hFHCSCHS_E10_STATE
                ,hFHCSCHS_ACT_E10_STATE
                ,hFHCSCHS_ACT_CHAMBER_STATE
                ,hFHCSCHS_NEW_E10_STATE
                ,hFHCSCHS_NEW_CHAMBER_STATE
                ,hFHCSCHS_NEW_ACT_E10_STATE
                ,hFHCSCHS_NEW_ACT_CHAMBER_STATE );

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.DurableReworkEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/23 10:17
     */
    public Infos.LotWaferSortEventRecord getEventData(String id) {
        String sql="Select * from OMEVWSORT where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.LotWaferSortEventRecord theEventData=new Infos.LotWaferSortEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.WaferEventData> currentWafers=new ArrayList<>();
        theEventData.setCurrentWafers(currentWafers);

        List<Infos.WaferEventData> sourceWafers=new ArrayList<>();
        theEventData.setSourceWafers(sourceWafers);

        for (Map<String,Object> sqlData:sqlDatas) {

            theEventData.setLotID(convert(sqlData.get("LOT_ID")));

            sql="SELECT * FROM OMEVWSORT_CURWF WHERE REFKEY=?";
            List<Map> sqlCurrentWafers = baseCore.queryAllForMap(sql, id);

            for (Map sqlCurrentWafer:sqlCurrentWafers){
                Infos.WaferEventData currentWafer=new Infos.WaferEventData();
                currentWafers.add(currentWafer);

                currentWafer.setWaferID(convert(sqlCurrentWafer.get("WAFER_ID")));
                currentWafer.setOriginalWaferID(convert(sqlCurrentWafer.get("ORIG_WAFER_ID")));
                currentWafer.setControlWaferFlag(convertB(sqlCurrentWafer.get("NPW_WAFER")));
                currentWafer.setOriginalCassetteID(convert(sqlCurrentWafer.get("ORIG_CARRIER_ID")));
                currentWafer.setOriginalSlotNumber(convertL(sqlCurrentWafer.get("ORIG_SLOT_NO")));
                currentWafer.setDestinationCassetteID(convert(sqlCurrentWafer.get("DEST_CARRIER_ID")));
                currentWafer.setDestinationSlotNumber(convertL(sqlCurrentWafer.get("DEST_SLOT_NO")));
            }
            sql="SELECT * FROM OMEVWSORT_SRCWF WHERE REFKEY=?";
            List<Map> sqlSourceWafers = baseCore.queryAllForMap(sql, id);

            for (Map sqlSourceWafer:sqlSourceWafers){
                Infos.WaferEventData sourceWafer=new Infos.WaferEventData();
                sourceWafers.add(sourceWafer);

                sourceWafer.setWaferID(convert(sqlSourceWafer.get("WAFER_ID")));
                sourceWafer.setOriginalWaferID(convert(sqlSourceWafer.get("ORIG_WAFER_ID")));
                sourceWafer.setControlWaferFlag(convertB(sqlSourceWafer.get("NPW_WAFER")));
                sourceWafer.setOriginalCassetteID(convert(sqlSourceWafer.get("ORIG_CARRIER_ID")));
                sourceWafer.setOriginalSlotNumber(convertL(sqlSourceWafer.get("ORIG_SLOT_NO")));
                sourceWafer.setDestinationCassetteID(convert(sqlSourceWafer.get("DEST_CARRIER_ID")));
                sourceWafer.setDestinationSlotNumber(convertL(sqlSourceWafer.get("DEST_SLOT_NO")));
            }

            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setSorterJobID(convert(sqlData.get("SORTER_JOB_ID")));
            theEventData.setComponentJobID(convert(sqlData.get("COMPONENT_JOB_ID")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

        }
        return theEventData;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param refKey
     * @return java.util.List<com.fa.cim.dto.Infos.UserDataSet>
     * @exception
     * @author Ho
     * @date 2019/7/23 10:28
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVWSORT_CDA WHERE REFKEY=?";
        List<Map> uDatas = baseCore.queryAllForMap(sql, refKey);
        List<Infos.UserDataSet> userDataSets=new ArrayList<>();
        for (Map<String,Object> uData:uDatas) {
            Infos.UserDataSet userDataSet=new Infos.UserDataSet();
            userDataSets.add(userDataSet);
            userDataSet.setName(convert(uData.get("NAME")));
            userDataSet.setType(convert(uData.get("TYPE")));
            userDataSet.setValue(convert(uData.get("VALUE")));
            userDataSet.setOriginator(convert(uData.get("ORIG")));
        }
        return userDataSets;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFIFO(String tableName,String refKey) {
        String sql=String.format("DELETE %s WHERE REFKEY=?",tableName);
        baseCore.insert(sql,refKey);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return com.fa.cim.Custom.List<java.lang.String>
     * @exception
     * @author Ho
     * @date 2019/4/19 17:41
     */
    public List<String> getEventFIFO(String tableName){
        String sql=String.format("SELECT REFKEY,EVENT_RKEY FROM %s WHERE TO_TIMESTAMP(EVENT_TIME,'yyyy-MM-dd-HH24.mi.SSxFF')<SYSDATE ORDER BY EVENT_TIME ASC",tableName);
        List<Object[]> fifos = baseCore.queryAll(sql);
        List<String> events=new ArrayList<>();
        fifos.forEach(fifo->events.add(convert(fifo[0])));
        return events;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotWaferScrapRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/24 14:57
     */
    public Response insertLotWaferScrapHistory( Infos.Ohscrhs lotWaferScrapRecord ) {
        String    hFHSCRHS_LOT_ID             ;
        String    hFHSCRHS_WAFER_ID           ;
        String    hFHSCRHS_REASON_CODE        ;
        String    hFHSCRHS_REASON_DESCRIPTION ;

        Integer hFHSCRHS_SCRAP_UNIT_COUNT;
        String    hFHSCRHS_PRODSPEC_ID        ;
        String    hFHSCRHS_LOT_OWNER_ID       ;
        String    hFHSCRHS_PRODGRP_ID         ;
        String    hFHSCRHS_TECH_ID            ;
        String    hFHSCRHS_CUSTPROD_ID        ;
        String    hFHSCRHS_ORDER_NO           ;
        String    hFHSCRHS_CUSTOMER_ID        ;

        Integer hFHSCRHS_CONTROL_WAFER;

        Integer hFHSCRHS_GOOD_UNIT_WAFER;

        Integer hFHSCRHS_REPAIR_UNIT_WAFER;

        Integer hFHSCRHS_FAIL_UNIT_WAFER;
        String    hFHSCRHS_LOT_TYPE           ;
        String    hFHSCRHS_CAST_ID            ;
        String    hFHSCRHS_CAST_CATEGORY      ;
        String    hFHSCRHS_PROD_TYPE          ;
        String    hFHSCRHS_CLAIM_MAINPD_ID    ;
        String    hFHSCRHS_CLAIM_OPE_NO       ;
        String    hFHSCRHS_CLAIM_PD_ID        ;

        Integer hFHSCRHS_CLAIM_PASS_COUNT;
        String    hFHSCRHS_CLAIM_OPE_NAME     ;
        String    hFHSCRHS_CLAIM_TEST_TYPE    ;
        String    hFHSCRHS_CLAIM_TIME         ;
        Double  hFHSCRHS_CLAIM_SHOP_DATE;
        String    hFHSCRHS_CLAIM_USER_ID      ;
        String    hFHSCRHS_CLAIM_STAGE_ID     ;
        String    hFHSCRHS_CLAIM_STAGEGRP_ID  ;
        String    hFHSCRHS_CLAIM_PHOTO_LAYER  ;
        String    hFHSCRHS_CLAIM_DEPARTMENT   ;
        String    hFHSCRHS_CLAIM_BANK_ID      ;
        String    hFHSCRHS_REASON_LOT_ID      ;
        String    hFHSCRHS_REASON_MAINPD_ID   ;
        String    hFHSCRHS_REASON_OPE_NO      ;
        String    hFHSCRHS_REASON_PD_ID       ;

        Integer hFHSCRHS_REASON_PASS_COUNT;
        String    hFHSCRHS_REASON_OPE_NAME    ;
        String    hFHSCRHS_REASON_TEST_TYPE   ;
        String    hFHSCRHS_REASON_STAGE_ID    ;
        String    hFHSCRHS_REASON_STAGEGRP_ID ;
        String    hFHSCRHS_REASON_PHOTO_LAYER ;
        String    hFHSCRHS_REASON_DEPARTMENT  ;
        String    hFHSCRHS_REASON_LOCATION_ID ;
        String    hFHSCRHS_REASON_AREA_ID     ;
        String    hFHSCRHS_REASON_EQP_ID      ;
        String    hFHSCRHS_REASON_EQP_NAME    ;
        String    hFHSCRHS_SCRAP_TYPE         ;
        String    hFHSCRHS_CLAIM_MEMO         ;
        String    hFHSCRHS_EVENT_CREATE_TIME  ;

        log.info("HistoryWatchDogServer::InsertLotWaferScrapHistory Function" );

        hFHSCRHS_LOT_ID=              lotWaferScrapRecord.getLot_id            ();
        hFHSCRHS_WAFER_ID=            lotWaferScrapRecord.getWafer_id          ();
        hFHSCRHS_REASON_CODE=         lotWaferScrapRecord.getReason_code       ();
        hFHSCRHS_REASON_DESCRIPTION=  lotWaferScrapRecord.getReason_description();
        hFHSCRHS_SCRAP_UNIT_COUNT =   lotWaferScrapRecord.getScrap_unit_count();
        hFHSCRHS_PRODSPEC_ID=         lotWaferScrapRecord.getProdspec_id       ();
        hFHSCRHS_LOT_OWNER_ID=        lotWaferScrapRecord.getLot_owner_id      ();
        hFHSCRHS_PRODGRP_ID=          lotWaferScrapRecord.getProdgrp_id        ();
        hFHSCRHS_TECH_ID=             lotWaferScrapRecord.getTech_id           ();
        hFHSCRHS_CUSTPROD_ID=         lotWaferScrapRecord.getCustprod_id       ();
        hFHSCRHS_ORDER_NO=            lotWaferScrapRecord.getOrder_no          ();
        hFHSCRHS_CUSTOMER_ID=         lotWaferScrapRecord.getCustomer_id       ();
        hFHSCRHS_CONTROL_WAFER =      lotWaferScrapRecord.getControl_wafer();
        hFHSCRHS_GOOD_UNIT_WAFER =    lotWaferScrapRecord.getGood_unit_wafer();
        hFHSCRHS_REPAIR_UNIT_WAFER =  lotWaferScrapRecord.getRepair_unit_wafer();
        hFHSCRHS_FAIL_UNIT_WAFER =    lotWaferScrapRecord.getFail_unit_wafer();
        hFHSCRHS_LOT_TYPE=            lotWaferScrapRecord.getLot_type          ();
        hFHSCRHS_CAST_ID=             lotWaferScrapRecord.getCast_id           ();
        hFHSCRHS_CAST_CATEGORY=       lotWaferScrapRecord.getCast_category      ();
        hFHSCRHS_PROD_TYPE=           lotWaferScrapRecord.getProd_type          ();
        hFHSCRHS_CLAIM_MAINPD_ID=     lotWaferScrapRecord.getClaim_mainpd_id   ();
        hFHSCRHS_CLAIM_OPE_NO=        lotWaferScrapRecord.getClaim_ope_no      ();
        hFHSCRHS_CLAIM_PD_ID=         lotWaferScrapRecord.getClaim_pd_id       ();
        hFHSCRHS_CLAIM_PASS_COUNT =   lotWaferScrapRecord.getClaim_pass_count();
        hFHSCRHS_CLAIM_OPE_NAME=      lotWaferScrapRecord.getClaim_ope_name    ();
        hFHSCRHS_CLAIM_TEST_TYPE=     lotWaferScrapRecord.getClaim_test_type   ();
        hFHSCRHS_CLAIM_TIME=          lotWaferScrapRecord.getClaim_time        ();
        hFHSCRHS_CLAIM_SHOP_DATE =    lotWaferScrapRecord.getClaim_shop_date();
        hFHSCRHS_CLAIM_USER_ID=       lotWaferScrapRecord.getClaim_user_id     ();
        hFHSCRHS_CLAIM_STAGE_ID=      lotWaferScrapRecord.getClaim_stage_id    ();
        hFHSCRHS_CLAIM_STAGEGRP_ID=   lotWaferScrapRecord.getClaim_stagegrp_id ();
        hFHSCRHS_CLAIM_PHOTO_LAYER=   lotWaferScrapRecord.getClaim_photo_layer ();
        hFHSCRHS_CLAIM_DEPARTMENT=    lotWaferScrapRecord.getClaim_department  ();
        hFHSCRHS_CLAIM_BANK_ID=       lotWaferScrapRecord.getClaim_bank_id     ();
        hFHSCRHS_REASON_LOT_ID=       lotWaferScrapRecord.getReason_lot_id     ();
        hFHSCRHS_REASON_MAINPD_ID=    lotWaferScrapRecord.getReason_mainpd_id  ();
        hFHSCRHS_REASON_OPE_NO=       lotWaferScrapRecord.getReason_ope_no     ();
        hFHSCRHS_REASON_PD_ID=        lotWaferScrapRecord.getReason_pd_id      ();
        hFHSCRHS_REASON_PASS_COUNT=   lotWaferScrapRecord.getReason_pass_count();
        hFHSCRHS_REASON_OPE_NAME=     lotWaferScrapRecord.getReason_ope_name   ();
        hFHSCRHS_REASON_TEST_TYPE=    lotWaferScrapRecord.getReason_test_type  ();
        hFHSCRHS_REASON_STAGE_ID=     lotWaferScrapRecord.getReason_stage_id   ();
        hFHSCRHS_REASON_STAGEGRP_ID=  lotWaferScrapRecord.getReason_stagegrp_id();
        hFHSCRHS_REASON_PHOTO_LAYER=  lotWaferScrapRecord.getReason_photo_layer();
        hFHSCRHS_REASON_DEPARTMENT=   lotWaferScrapRecord.getReason_department ();
        hFHSCRHS_REASON_LOCATION_ID=  lotWaferScrapRecord.getReason_location_id();
        hFHSCRHS_REASON_AREA_ID=      lotWaferScrapRecord.getReason_area_id    ();
        hFHSCRHS_REASON_EQP_ID=       lotWaferScrapRecord.getReason_eqp_id     ();
        hFHSCRHS_REASON_EQP_NAME=     lotWaferScrapRecord.getReason_eqp_name   ();
        hFHSCRHS_SCRAP_TYPE=          lotWaferScrapRecord.getScrap_type        ();
        hFHSCRHS_CLAIM_MEMO=          lotWaferScrapRecord.getClaim_memo        ();
        hFHSCRHS_EVENT_CREATE_TIME=   lotWaferScrapRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHSCRAP\n" +
                "                (ID,  LOT_ID,\n" +
                "                        WAFER_ID,\n" +
                "                        REASON_CODE,\n" +
                "                        REASON_DESC,\n" +
                "                        SCRAP_UNIT_COUNT,\n" +
                "                        PROD_ID,\n" +
                "                        LOT_OWNER_ID,\n" +
                "                        PRODFMLY_ID,\n" +
                "                        TECH_ID,\n" +
                "                        CUSTPROD_ID,\n" +
                "                        MFG_ORDER_NO,\n" +
                "                        CUSTOMER_ID,\n" +
                "                        NPW_WAFER,\n" +
                "                        GOOD_UNIT_WAFER,\n" +
                "                        REPAIR_UNIT_WAFER,\n" +
                "                        FAIL_UNIT_WAFER,\n" +
                "                        LOT_TYPE,\n" +
                "                        CARRIER_ID,\n" +
                "                        CARRIER_CATEGORY,\n" +
                "                        PROD_TYPE,\n" +
                "                        PROCESS_ID,\n" +
                "                        OPE_NO,\n" +
                "                        STEP_ID,\n" +
                "                        PASS_COUNT,\n" +
                "                        OPE_NAME,\n" +
                "                        TEST_TYPE,\n" +
                "                        TRX_TIME,\n" +
                "                        TRX_WORK_DATE,\n" +
                "                        TRX_USER_ID,\n" +
                "                        STAGE_ID,\n" +
                "                        STAGE_GRP_ID,\n" +
                "                        PHOTO_LAYER,\n" +
                "                        DEPARTMENT,\n" +
                "                        BANK_ID,\n" +
                "                        REASON_LOT_ID,\n" +
                "                        REASON_PROCESS_ID,\n" +
                "                        REASON_OPE_NO,\n" +
                "                        REASON_STEP_ID,\n" +
                "                        REASON_PASS_COUNT,\n" +
                "                        REASON_OPE_NAME,\n" +
                "                        REASON_TEST_TYPE,\n" +
                "                        REASON_STAGE_ID,\n" +
                "                        REASON_STAGE_GRP_ID,\n" +
                "                        REASON_PHOTO_LAYER,\n" +
                "                        REASON_DEPARTMENT,\n" +
                "                        REASON_LOCATION_ID,\n" +
                "                        REASON_BAY_ID,\n" +
                "                        REASON_EQP_ID,\n" +
                "                        REASON_EQP_NAME,\n" +
                "                        SCRAP_TYPE,\n" +
                "                        TRX_MEMO,\n" +
                "                        EVENT_CREATE_TIME,\n" +
                "                        STORE_TIME )\n" +
                "        Values\n" +
                "                (?,  ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                    ?,\n" +
                "                CURRENT_TIMESTAMP )",generateID(Infos.Ohscrhs.class)
                ,hFHSCRHS_LOT_ID
                ,hFHSCRHS_WAFER_ID
                ,hFHSCRHS_REASON_CODE
                ,hFHSCRHS_REASON_DESCRIPTION
                ,hFHSCRHS_SCRAP_UNIT_COUNT
                ,hFHSCRHS_PRODSPEC_ID
                ,hFHSCRHS_LOT_OWNER_ID
                ,hFHSCRHS_PRODGRP_ID
                ,hFHSCRHS_TECH_ID
                ,hFHSCRHS_CUSTPROD_ID
                ,hFHSCRHS_ORDER_NO
                ,hFHSCRHS_CUSTOMER_ID
                ,hFHSCRHS_CONTROL_WAFER
                ,hFHSCRHS_GOOD_UNIT_WAFER
                ,hFHSCRHS_REPAIR_UNIT_WAFER
                ,hFHSCRHS_FAIL_UNIT_WAFER
                ,hFHSCRHS_LOT_TYPE
                ,hFHSCRHS_CAST_ID
                ,hFHSCRHS_CAST_CATEGORY
                ,hFHSCRHS_PROD_TYPE
                ,hFHSCRHS_CLAIM_MAINPD_ID
                ,hFHSCRHS_CLAIM_OPE_NO
                ,hFHSCRHS_CLAIM_PD_ID
                ,hFHSCRHS_CLAIM_PASS_COUNT
                ,hFHSCRHS_CLAIM_OPE_NAME
                ,hFHSCRHS_CLAIM_TEST_TYPE
                ,convert(hFHSCRHS_CLAIM_TIME)
                ,hFHSCRHS_CLAIM_SHOP_DATE
                ,hFHSCRHS_CLAIM_USER_ID
                ,hFHSCRHS_CLAIM_STAGE_ID
                ,hFHSCRHS_CLAIM_STAGEGRP_ID
                ,hFHSCRHS_CLAIM_PHOTO_LAYER
                ,hFHSCRHS_CLAIM_DEPARTMENT
                ,hFHSCRHS_CLAIM_BANK_ID
                ,hFHSCRHS_REASON_LOT_ID
                ,hFHSCRHS_REASON_MAINPD_ID
                ,hFHSCRHS_REASON_OPE_NO
                ,hFHSCRHS_REASON_PD_ID
                ,hFHSCRHS_REASON_PASS_COUNT
                ,hFHSCRHS_REASON_OPE_NAME
                ,hFHSCRHS_REASON_TEST_TYPE
                ,hFHSCRHS_REASON_STAGE_ID
                ,hFHSCRHS_REASON_STAGEGRP_ID
                ,hFHSCRHS_REASON_PHOTO_LAYER
                ,hFHSCRHS_REASON_DEPARTMENT
                ,hFHSCRHS_REASON_LOCATION_ID
                ,hFHSCRHS_REASON_AREA_ID
                ,hFHSCRHS_REASON_EQP_ID
                ,hFHSCRHS_REASON_EQP_NAME
                ,hFHSCRHS_SCRAP_TYPE
                ,hFHSCRHS_CLAIM_MEMO
                ,convert(hFHSCRHS_EVENT_CREATE_TIME));


        log.info("HistoryWatchDogServer::InsertLotWaferScrapHistory Function" );
        return( returnOK() );
    }

}
