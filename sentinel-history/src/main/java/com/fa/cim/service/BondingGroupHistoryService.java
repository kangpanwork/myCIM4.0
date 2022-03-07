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
public class BondingGroupHistoryService {

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
                ,convert(hFHCSCHS_END_TIME)
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
    public Infos.BondingGroupEventRecord getEventData(String id) {
        String sql="Select * from OMEVBONDGRP where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.BondingGroupEventRecord theEventData=new Infos.BondingGroupEventRecord();
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);

        List<Infos.BondingMapEventData> bondingMapInfos=new ArrayList<>();
        theEventData.setBondingMapInfos(bondingMapInfos);

        for (Map<String,Object> sqlData:sqlDatas) {
            theEventData.setBondingGroupID(convert(sqlData.get("BOND_GRP_ID")));
            theEventData.setAction(convert(sqlData.get("TASK_TYPE")));
            theEventData.setBondingGroupStatus(convert(sqlData.get("BOND_GRP_STATUS")));
            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setControlJobID(convert(sqlData.get("CJ_ID")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            sql="SELECT * FROM OMEVBONDGRP_MAP WHERE REFKEY=?";
            List<Map> sqlBondingMapInfos = baseCore.queryAllForMap(sql, id);

            for (Map sqlBondingMapInfo:sqlBondingMapInfos){
                Infos.BondingMapEventData bondingMapInfo=new Infos.BondingMapEventData();
                bondingMapInfos.add(bondingMapInfo);

                bondingMapInfo.setBondingGroupID(convert(sqlBondingMapInfo.get("BOND_GRP_ID")));
                bondingMapInfo.setAction(convert(sqlBondingMapInfo.get("TASK_TYPE")));
                bondingMapInfo.setBondingSeqNo(convertL(sqlBondingMapInfo.get("BOND_IDX_NO")));
                bondingMapInfo.setBaseLotID(convert(sqlBondingMapInfo.get("BASE_LOT_ID")));
                bondingMapInfo.setBaseProductID(convert(sqlBondingMapInfo.get("BASE_PROD_ID")));
                bondingMapInfo.setBaseWaferID(convert(sqlBondingMapInfo.get("BASE_WAFER_ID")));
                bondingMapInfo.setBaseBondingSide(convert(sqlBondingMapInfo.get("BASE_BOND_SIDE")));
                bondingMapInfo.setTopLotID(convert(sqlBondingMapInfo.get("BASE_PROD_ID")));
                bondingMapInfo.setTopProductID(convert(sqlBondingMapInfo.get("TOP_PROD_ID")));
                bondingMapInfo.setTopWaferID(convert(sqlBondingMapInfo.get("TOP_WAFER_ID")));
                bondingMapInfo.setTopBondingSide(convert(sqlBondingMapInfo.get("TOP_BOND_SIDE")));
            }
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
        String sql="SELECT * FROM OMEVBONDGRP_CDA  WHERE REFKEY=?";
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
     * @param bondingGroupEventRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:31
     */
    public Response insertBondingGroupHistory( Infos.Ohbgrphs bondingGroupEventRecord ) {
        String     hFHBGRPHS_BOND_GRP_ID    ;
        String     hFHBGRPHS_ACTION         ;
        String     hFHBGRPHS_BOND_GRP_STATUS;
        String     hFHBGRPHS_EQP_ID         ;
        String     hFHBGRPHS_CTRLJOB_ID     ;
        String     hFHBGRPHS_TX_ID          ;
        String     hFHBGRPHS_CLAIM_TIME     ;
        String     hFHBGRPHS_CLAIM_USER_ID  ;
        String     hFHBGRPHS_CLAIM_MEMO     ;
        String     hFHBGRPHS_EVENT_CREATE_TIME ;

        log.info("HistoryWatchDogServer::InsertBondingGroupHistory Function" );

        hFHBGRPHS_BOND_GRP_ID=       bondingGroupEventRecord.getBondingGroupID();
        hFHBGRPHS_ACTION=            bondingGroupEventRecord.getAction();
        hFHBGRPHS_BOND_GRP_STATUS=   bondingGroupEventRecord.getBondingGroupStatus();
        hFHBGRPHS_EQP_ID=            bondingGroupEventRecord.getEquipmentID();
        hFHBGRPHS_CTRLJOB_ID=        bondingGroupEventRecord.getControlJobID();
        hFHBGRPHS_TX_ID=             bondingGroupEventRecord.getTx_id();
        hFHBGRPHS_CLAIM_TIME=        bondingGroupEventRecord.getClaim_time();
        hFHBGRPHS_CLAIM_USER_ID=     bondingGroupEventRecord.getClaim_user_id();
        hFHBGRPHS_CLAIM_MEMO=        bondingGroupEventRecord.getClaim_memo();
        hFHBGRPHS_EVENT_CREATE_TIME= bondingGroupEventRecord.getEvent_create_time() ;

        baseCore.insert("INSERT INTO OHBONDGRP\n"+
                        "(ID,  BOND_GRP_ID,\n"+
                        "TASK_TYPE,\n"+
                        "BOND_GRP_STATUS,\n"+
                        "EQP_ID,\n"+
                        "CJ_ID,\n"+
                        "TRX_ID,\n"+
                        "TRX_TIME,\n"+
                        "TRX_USER_ID,\n"+
                        "TRX_MEMO,\n"+
                        "STORE_TIME,\n"+
                        "EVENT_CREATE_TIME)\n"+
                        "Values\n"+
                        "(?,  ?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "CURRENT_TIMESTAMP,\n"+
                        "?)",generateID(Infos.Ohbgrphs.class),
                hFHBGRPHS_BOND_GRP_ID,
                hFHBGRPHS_ACTION,
                hFHBGRPHS_BOND_GRP_STATUS,
                hFHBGRPHS_EQP_ID,
                hFHBGRPHS_CTRLJOB_ID,
                hFHBGRPHS_TX_ID,
                convert(hFHBGRPHS_CLAIM_TIME),
                hFHBGRPHS_CLAIM_USER_ID,
                hFHBGRPHS_CLAIM_MEMO,
                convert(hFHBGRPHS_EVENT_CREATE_TIME));

        log.info("HistoryWatchDogServer::InsertBondingGroupHistory Function" );
        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param bondingGroupEventRecord_map
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:35
     */
    public Response insertBondingGroupHistory_map( Infos.OhbgrphsMap bondingGroupEventRecord_map ) {
        String     hFHBGRPHS_MAP_BOND_GRP_ID      ;
        String     hFHBGRPHS_MAP_ACTION           ;
        Integer hFHBGRPHS_MAP_BOND_SEQ_NO;
        String     hFHBGRPHS_MAP_BASE_LOT_ID      ;
        String     hFHBGRPHS_MAP_BASE_PRODSPEC_ID ;
        String     hFHBGRPHS_MAP_BASE_WAFER_ID    ;
        String     hFHBGRPHS_MAP_BASE_BOND_SIDE   ;
        String     hFHBGRPHS_MAP_TOP_LOT_ID       ;
        String     hFHBGRPHS_MAP_TOP_PRODSPEC_ID  ;
        String     hFHBGRPHS_MAP_TOP_WAFER_ID     ;
        String     hFHBGRPHS_MAP_TOP_BOND_SIDE    ;
        String     hFHBGRPHS_MAP_CLAIM_TIME       ;

        log.info("HistoryWatchDogServer::InsertBondingGroupHistory_map Function" );

        hFHBGRPHS_MAP_BOND_GRP_ID=       bondingGroupEventRecord_map.getBondingGroupID();
        hFHBGRPHS_MAP_ACTION=            bondingGroupEventRecord_map.getAction();
        hFHBGRPHS_MAP_BOND_SEQ_NO =      bondingGroupEventRecord_map.getBondingSeqNo();
        hFHBGRPHS_MAP_BASE_LOT_ID=       bondingGroupEventRecord_map.getBaseLotID();
        hFHBGRPHS_MAP_BASE_PRODSPEC_ID=  bondingGroupEventRecord_map.getBaseProductID();
        hFHBGRPHS_MAP_BASE_WAFER_ID=     bondingGroupEventRecord_map.getBaseWaferID();
        hFHBGRPHS_MAP_BASE_BOND_SIDE=    bondingGroupEventRecord_map.getBaseBondingSide();
        hFHBGRPHS_MAP_TOP_LOT_ID=        bondingGroupEventRecord_map.getTopLotID();
        hFHBGRPHS_MAP_TOP_PRODSPEC_ID=   bondingGroupEventRecord_map.getTopProductID();
        hFHBGRPHS_MAP_TOP_WAFER_ID=      bondingGroupEventRecord_map.getTopWaferID();
        hFHBGRPHS_MAP_TOP_BOND_SIDE=     bondingGroupEventRecord_map.getTopBondingSide();
        hFHBGRPHS_MAP_CLAIM_TIME=        bondingGroupEventRecord_map.getClaim_time();

        baseCore.insert("INSERT INTO OHBONDGRP_MAP\n"+
                        "(ID,  BOND_GRP_ID,\n"+
                        "TASK_TYPE,\n"+
                        "BOND_IDX_NO,\n"+
                        "BASE_LOT_ID,\n"+
                        "BASE_PROD_ID,\n"+
                        "BASE_WAFER_ID,\n"+
                        "BASE_BOND_SIDE,\n"+
                        "TOP_LOT_ID,\n"+
                        "TOP_PROD_ID,\n"+
                        "TOP_WAFER_ID,\n"+
                        "TOP_BOND_SIDE,\n"+
                        "TRX_TIME )\n"+
                        "Values\n"+
                        "(?,  ?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "?,\n"+
                        "? )",generateID(Infos.OhbgrphsMap.class),
                hFHBGRPHS_MAP_BOND_GRP_ID,
                hFHBGRPHS_MAP_ACTION,
                hFHBGRPHS_MAP_BOND_SEQ_NO,
                hFHBGRPHS_MAP_BASE_LOT_ID,
                hFHBGRPHS_MAP_BASE_PRODSPEC_ID,
                hFHBGRPHS_MAP_BASE_WAFER_ID,
                hFHBGRPHS_MAP_BASE_BOND_SIDE,
                hFHBGRPHS_MAP_TOP_LOT_ID,
                hFHBGRPHS_MAP_TOP_PRODSPEC_ID,
                hFHBGRPHS_MAP_TOP_WAFER_ID,
                hFHBGRPHS_MAP_TOP_BOND_SIDE,
                convert(hFHBGRPHS_MAP_CLAIM_TIME));

        log.info("HistoryWatchDogServer::InsertBondingGroupHistory_map Function" );
        return( returnOK() );
    }

}
