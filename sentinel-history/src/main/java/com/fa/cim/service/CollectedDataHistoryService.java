package com.fa.cim.service;

import com.fa.cim.Constant.SPConstant;
import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @date 2019/5/11 15:24
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class CollectedDataHistoryService {

    @Autowired
    private BaseCore baseCore;

    @Autowired
    private TableMethod tableMethod;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.CollectedDataEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/15 14:44
     */
    public Infos.CollectedDataEventRecord getEventData(String id) {
        String sql="Select * from OMEVEDC where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.CollectedDataEventRecord theEventData = new Infos.CollectedDataEventRecord();
        Infos.LotEventData measuredLotData=new Infos.LotEventData();
        theEventData.setMeasuredLotData(measuredLotData);
        List<Infos.ProcessedLotEventData> processedLots=new ArrayList<>();
        theEventData.setProcessedLots(processedLots);
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            measuredLotData.setLotID(convert(sqlData.get("LOT_ID")));
            measuredLotData.setLotType(convert(sqlData.get("LOT_TYPE")));
            measuredLotData.setCassetteID(convert(sqlData.get("CARRIER_ID")));
            measuredLotData.setLotStatus(convert(sqlData.get("LOT_STATUS")));
            measuredLotData.setCustomerID(convert(sqlData.get("CUSTOMER_ID")));
            measuredLotData.setPriorityClass(convertL(sqlData.get("LOT_PRIORITY")));
            measuredLotData.setProductID(convert(sqlData.get("PROD_ID")));
            measuredLotData.setOriginalWaferQuantity(convertI(sqlData.get("ORIGINAL_QTY")));
            measuredLotData.setCurrentWaferQuantity(convertI(sqlData.get("CUR_QTY")));
            measuredLotData.setProductWaferQuantity(convertI(sqlData.get("PROD_QTY")));
            measuredLotData.setControlWaferQuantity(convertI(sqlData.get("NPW_QTY")));
            measuredLotData.setHoldState(convert(sqlData.get("LOT_HOLD_STATE")));
            measuredLotData.setBankID(convert(sqlData.get("BANK_ID")));
            measuredLotData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            measuredLotData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            measuredLotData.setOperationID(convert(sqlData.get("STEP_ID")));
            measuredLotData.setOperationPassCount(convertI(sqlData.get("PASS_COUNT")));
            measuredLotData.setObjrefPOS(convert(sqlData.get("PRSS_RKEY")));
            measuredLotData.setWaferHistoryTimeStamp(convert(sqlData.get("WAFER_HIS_TIME")));
            measuredLotData.setObjrefPO(convert(sqlData.get("PROPE_RKEY")));
            measuredLotData.setObjrefMainPF(convert(sqlData.get("MROUTE_PRF_RKEY")));
            measuredLotData.setObjrefModulePOS(convert(sqlData.get("ROUTE_PRSS_RKEY")));

            sql="SELECT * FROM OMEVEDC_LOT WHERE REFKEY=?";
            List<Map> sqlProcessedLots=baseCore.queryAllForMap(sql,id);

            for (Map sqlProcessedLot:sqlProcessedLots) {
                Infos.ProcessedLotEventData processedLot=new Infos.ProcessedLotEventData();
                processedLots.add(processedLot);

                processedLot.setProcessLotID(convert(sqlProcessedLot.get("LOT_ID")));
                processedLot.setProcessRouteID(convert(sqlProcessedLot.get("PROCESS_ID")));
                processedLot.setProcessOperationNumber(convert(sqlProcessedLot.get("OPE_NO")));
                processedLot.setProcessOperationPassCount(convertL(sqlProcessedLot.get("PASS_COUNT")));
                processedLot.setProcessObjrefPO(convert(sqlProcessedLot.get("PROPE_RKEY")));
            }

            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setLogicalRecipeID(convert(sqlData.get("LRCP_ID")));
            theEventData.setMachineRecipeID(convert(sqlData.get("MRCP_ID")));
            theEventData.setPhysicalRecipeID(convert(sqlData.get("PRCP_ID")));
            theEventData.setMonitorGroupID(convert(sqlData.get("MON_GRP_ID")));

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
     * @date 2019/6/15 14:46
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVEDC_CDA WHERE REFKEY=?";
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

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param seqNo
     * @param frpo_actions
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 16:28
     */
    public Response fetchFRPO_ACTIONS(Params.Param<Integer> seqNo, Infos.FrpoActions frpo_actions , Object[] FRPO_ACTION) {
        Integer hFRPO_ACTIONS_d_SeqNo;
        String hFRPO_ACTIONS_LOT_ID;
        Boolean    hFRPO_ACTIONS_MLOT_FLG;
        String hFRPO_ACTIONS_DCDEF_ID;
        String hFRPO_ACTIONS_DCSPEC_ID;
        String hFRPO_ACTIONS_CHECK_TYPE;
        String hFRPO_ACTIONS_REASON_CODE;
        String hFRPO_ACTIONS_ACT_CODE;
        String hFRPO_ACTIONS_PROC_MAINPD_ID;
        String hFRPO_ACTIONS_PROC_OPE_NO;
        String hFRPO_ACTIONS_PROC_PD_ID;
        Integer hFRPO_ACTIONS_PROC_PASS_COUNT;
        String hFRPO_ACTIONS_PROC_EQP_ID;
        String hFRPO_ACTIONS_PROC_RECIPE_ID;
        String hFRPO_ACTIONS_PROC_FAB_ID;
        String hFRPO_ACTIONS_BANK_ID;
        String hFRPO_ACTIONS_REWORK_ROUTE_ID;


        hFRPO_ACTIONS_d_SeqNo = convertI(FRPO_ACTION[0]);
        hFRPO_ACTIONS_LOT_ID = convert(FRPO_ACTION[1]);
        hFRPO_ACTIONS_MLOT_FLG = convertB(FRPO_ACTION[2]);
        hFRPO_ACTIONS_DCDEF_ID = convert(FRPO_ACTION[3]);
        hFRPO_ACTIONS_DCSPEC_ID = convert(FRPO_ACTION[4]);
        hFRPO_ACTIONS_CHECK_TYPE = convert(FRPO_ACTION[5]);
        hFRPO_ACTIONS_REASON_CODE = convert(FRPO_ACTION[6]);
        hFRPO_ACTIONS_ACT_CODE = convert(FRPO_ACTION[7]);
        hFRPO_ACTIONS_PROC_MAINPD_ID = convert(FRPO_ACTION[8]);
        hFRPO_ACTIONS_PROC_OPE_NO = convert(FRPO_ACTION[9]);
        hFRPO_ACTIONS_PROC_PD_ID = convert(FRPO_ACTION[10]);
        hFRPO_ACTIONS_PROC_PASS_COUNT = convertI(FRPO_ACTION[11]);
        hFRPO_ACTIONS_PROC_EQP_ID = convert(FRPO_ACTION[12]);
        hFRPO_ACTIONS_PROC_RECIPE_ID = convert(FRPO_ACTION[13]);
        hFRPO_ACTIONS_PROC_FAB_ID = convert(FRPO_ACTION[14]);
        hFRPO_ACTIONS_BANK_ID = convert(FRPO_ACTION[15]);
        hFRPO_ACTIONS_REWORK_ROUTE_ID = convert(FRPO_ACTION[16]);


        seqNo.setValue(hFRPO_ACTIONS_d_SeqNo);
        frpo_actions.setLot_id(hFRPO_ACTIONS_LOT_ID);
        frpo_actions.setMlot_flg (hFRPO_ACTIONS_MLOT_FLG);
        frpo_actions.setDcdef_id(hFRPO_ACTIONS_DCDEF_ID);
        frpo_actions.setDcspec_id(hFRPO_ACTIONS_DCSPEC_ID);
        frpo_actions.setCheck_type(hFRPO_ACTIONS_CHECK_TYPE);
        frpo_actions.setReason_code(hFRPO_ACTIONS_REASON_CODE);
        frpo_actions.setAct_code(hFRPO_ACTIONS_ACT_CODE);
        frpo_actions.setProc_mainpd_id(hFRPO_ACTIONS_PROC_MAINPD_ID);
        frpo_actions.setProc_ope_no(hFRPO_ACTIONS_PROC_OPE_NO);
        frpo_actions.setProc_pd_id(hFRPO_ACTIONS_PROC_PD_ID);
        frpo_actions.setProc_pass_count (hFRPO_ACTIONS_PROC_PASS_COUNT);
        frpo_actions.setProc_eqp_id(hFRPO_ACTIONS_PROC_EQP_ID);
        frpo_actions.setProc_recipe_id(hFRPO_ACTIONS_PROC_RECIPE_ID);
        frpo_actions.setProc_fab_id(hFRPO_ACTIONS_PROC_FAB_ID);
        frpo_actions.setBank_id(hFRPO_ACTIONS_BANK_ID);
        frpo_actions.setRework_route_id(hFRPO_ACTIONS_REWORK_ROUTE_ID);

        Response iRc=returnOK();

        return iRc;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param measuredObjrefPO
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 16:25
     */
    public Response selectFRPO_ACTIONS(String measuredObjrefPO){
        String hFRPO_PO_OBJ7;


        hFRPO_PO_OBJ7 = measuredObjrefPO ;

        List<Object[]> FRPO_ACTIONS=baseCore.queryAll("select  PA.IDX_NO,\n" +
                "                PA.LOT_ID,\n" +
                "                PA.MON_LOT_FLAG,\n" +
                "                PA.EDC_PLAN_ID,\n" +
                "                PA.EDC_SPEC_ID,\n" +
                "                PA.CHECK_TYPE,\n" +
                "                PA.REASON_CODE,\n" +
                "                PA.ACTION_CODE,\n" +
                "                coalesce( P2.MAIN_PROCESS_ID, '' )      as PROC_PROCESS_ID,\n" +
                "        coalesce( P2.OPE_NO, '' )         as PROC_OPE_NO,\n" +
                "        coalesce( P2.STEP_ID, '' )          as PROC_STEP_ID,\n" +
                "        coalesce( P2.PASS_COUNT, 0 )      as PROC_PASS_COUNT,\n" +
                "        coalesce( P2.ALLOC_EQP_ID, '' )    as PROC_EQP_ID,\n" +
                "        coalesce( P2.ALLOC_MRCP_ID, '' ) as PROC_MRCP_ID,\n" +
                "        coalesce( P2.FAB_ID, '' )         as PROC_FAB_ID,\n" +
                "        PA.BANK_ID,\n" +
                "                PA.REWORK_FLOW_ID\n" +
                "        from            OMPROPE            PO\n" +
                "        inner   join    OMPROPE_ACT    PA on PO.ID = PA.REFKEY\n" +
                "        left    join    OMPROPE            P2 on PA.CORR_PROPE_RKEY  = P2.ID\n" +
                "        where   PO.ID = ?\n" +
                "        order   by PA.IDX_NO",hFRPO_PO_OBJ7);


        Response iRc=returnOK();
        iRc.setBody(FRPO_ACTIONS);

        return iRc;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataActionRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 16:46
     */
    public Response insertCollectedDataActionHistory(Infos.OhcdatahsAction collectedDataActionRecord) {
        String hFHCDATAHS_ACTIONS_MEAS_LOT_ID       ;
        String hFHCDATAHS_ACTIONS_MEAS_MAINPD_ID    ;
        String hFHCDATAHS_ACTIONS_MEAS_OPE_NO       ;
        Integer hFHCDATAHS_ACTIONS_MEAS_PASS_COUNT;
        String hFHCDATAHS_ACTIONS_CLAIM_TIME        ;
        String hFHCDATAHS_ACTIONS_MEAS_DCDEF_ID     ;
        String hFHCDATAHS_ACTIONS_MEAS_DCSPEC_ID    ;
        String hFHCDATAHS_ACTIONS_CHECK_TYPE        ;
        String hFHCDATAHS_ACTIONS_REASON_CODE       ;
        String hFHCDATAHS_ACTIONS_ACT_CODE          ;
        String hFHCDATAHS_ACTIONS_PROC_LOT_ID       ;
        Boolean hFHCDATAHS_ACTIONS_MLOT_FLG;
        String hFHCDATAHS_ACTIONS_PROC_MAINPD_ID    ;
        String hFHCDATAHS_ACTIONS_PROC_OPE_NO       ;
        String hFHCDATAHS_ACTIONS_PROC_PD_ID        ;
        Integer hFHCDATAHS_ACTIONS_PROC_PASS_COUNT;
        String hFHCDATAHS_ACTIONS_PROC_EQP_ID       ;
        String hFHCDATAHS_ACTIONS_PROC_RECIPE_ID    ;
        String hFHCDATAHS_ACTIONS_PROC_PH_RECIPE_ID ;
        String hFHCDATAHS_ACTIONS_PROC_FAB_ID       ;
        String hFHCDATAHS_ACTIONS_BANK_ID           ;
        String hFHCDATAHS_ACTIONS_REWORK_ROUTE_ID   ;


        hFHCDATAHS_ACTIONS_MEAS_LOT_ID = collectedDataActionRecord.getMeas_lot_id ();
        hFHCDATAHS_ACTIONS_MEAS_MAINPD_ID = collectedDataActionRecord.getMeas_mainpd_id ();
        hFHCDATAHS_ACTIONS_MEAS_OPE_NO = collectedDataActionRecord.getMeas_ope_no ();
        hFHCDATAHS_ACTIONS_MEAS_PASS_COUNT = collectedDataActionRecord.getMeas_pass_count();
        hFHCDATAHS_ACTIONS_CLAIM_TIME = collectedDataActionRecord.getClaim_time ();
        hFHCDATAHS_ACTIONS_MEAS_DCDEF_ID = collectedDataActionRecord.getMeas_dcdef_id ();
        hFHCDATAHS_ACTIONS_MEAS_DCSPEC_ID = collectedDataActionRecord.getMeas_dcspec_id ();
        hFHCDATAHS_ACTIONS_CHECK_TYPE = collectedDataActionRecord.getCheck_type ();
        hFHCDATAHS_ACTIONS_REASON_CODE = collectedDataActionRecord.getReason_code ();
        hFHCDATAHS_ACTIONS_ACT_CODE = collectedDataActionRecord.getAct_code ();
        hFHCDATAHS_ACTIONS_PROC_LOT_ID = collectedDataActionRecord.getProc_lot_id ();
        hFHCDATAHS_ACTIONS_MLOT_FLG =        collectedDataActionRecord.getMlot_flg();
        hFHCDATAHS_ACTIONS_PROC_MAINPD_ID = collectedDataActionRecord.getProc_mainpd_id ();
        hFHCDATAHS_ACTIONS_PROC_OPE_NO = collectedDataActionRecord.getProc_ope_no ();
        hFHCDATAHS_ACTIONS_PROC_PD_ID = collectedDataActionRecord.getProc_pd_id ();
        hFHCDATAHS_ACTIONS_PROC_PASS_COUNT = collectedDataActionRecord.getProc_pass_count();
        hFHCDATAHS_ACTIONS_PROC_EQP_ID = collectedDataActionRecord.getProc_eqp_id ();
        hFHCDATAHS_ACTIONS_PROC_RECIPE_ID = collectedDataActionRecord.getProc_recipe_id ();
        hFHCDATAHS_ACTIONS_PROC_FAB_ID = collectedDataActionRecord.getProc_fab_id ();
        hFHCDATAHS_ACTIONS_BANK_ID = collectedDataActionRecord.getBank_id ();
        hFHCDATAHS_ACTIONS_REWORK_ROUTE_ID = collectedDataActionRecord.getRework_route_id ();


        baseCore.insert("INSERT INTO OHEDC_ACT\n" +
                "                (  ID,MEAS_LOT_ID,\n" +
                "                        MEAS_PROCESS_ID,\n" +
                "                        MEAS_OPE_NO,\n" +
                "                        MEAS_PASS_COUNT,\n" +
                "                        TRX_TIME,\n" +
                "                        MEAS_EDC_PLAN_ID,\n" +
                "                        MEAS_EDC_SPEC_ID,\n" +
                "                        CHECK_TYPE,\n" +
                "                        REASON_CODE,\n" +
                "                        ACTION_CODE,\n" +
                "                        PROC_LOT_ID,\n" +
                "                        MON_LOT_FLAG,\n" +
                "                        PROC_PROCESS_ID,\n" +
                "                        PROC_OPE_NO,\n" +
                "                        PROC_STEP_ID,\n" +
                "                        PROC_PASS_COUNT,\n" +
                "                        PROC_EQP_ID,\n" +
                "                        PROC_MRCP_ID,\n" +
                "                        PROC_FAB_ID,\n" +
                "                        BANK_ID,\n" +
                "                        REWORK_FLOW_ID,\n" +
                "                        STORE_TIME )\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                CURRENT_TIMESTAMP )",generateID(Infos.OhcdatahsAction.class)
                ,hFHCDATAHS_ACTIONS_MEAS_LOT_ID
                , hFHCDATAHS_ACTIONS_MEAS_MAINPD_ID
                , hFHCDATAHS_ACTIONS_MEAS_OPE_NO
                , hFHCDATAHS_ACTIONS_MEAS_PASS_COUNT
                , convert(hFHCDATAHS_ACTIONS_CLAIM_TIME)
                , hFHCDATAHS_ACTIONS_MEAS_DCDEF_ID
                , hFHCDATAHS_ACTIONS_MEAS_DCSPEC_ID
                , hFHCDATAHS_ACTIONS_CHECK_TYPE
                , hFHCDATAHS_ACTIONS_REASON_CODE
                , hFHCDATAHS_ACTIONS_ACT_CODE
                , hFHCDATAHS_ACTIONS_PROC_LOT_ID
                , hFHCDATAHS_ACTIONS_MLOT_FLG
                , hFHCDATAHS_ACTIONS_PROC_MAINPD_ID
                , hFHCDATAHS_ACTIONS_PROC_OPE_NO
                , hFHCDATAHS_ACTIONS_PROC_PD_ID
                , hFHCDATAHS_ACTIONS_PROC_PASS_COUNT
                , hFHCDATAHS_ACTIONS_PROC_EQP_ID
                , hFHCDATAHS_ACTIONS_PROC_RECIPE_ID
                , hFHCDATAHS_ACTIONS_PROC_FAB_ID
                , hFHCDATAHS_ACTIONS_BANK_ID
                , hFHCDATAHS_ACTIONS_REWORK_ROUTE_ID);


        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param measuredObjrefPO
     * @param seqNo
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 16:48
     */
    public Response selectFRPO_ACTIONS_ENTITY(String measuredObjrefPO, Params.Param<Integer> seqNo ) {
        String hFRPO_PO_OBJ8;
        String hFRPO_ACTIONS_ENTITY_d_theTableMarker;


        hFRPO_PO_OBJ8= measuredObjrefPO ;
        hFRPO_ACTIONS_ENTITY_d_theTableMarker =String.format(
                "%d", seqNo.getValue() );


        List<Object[] > FRPO_ACTIONS_ENT=baseCore.queryAll("select  AE.CLASS_NAME,\n" +
                "                AE.CLASS_ID,\n" +
                "                AE.CLASS_ATTRIB\n" +
                "        from            OMPROPE                    PO\n" +
                "        inner   join    OMPROPE_ACT_ENTITY     AE on PO.ID = AE.REFKEY\n" +
                "        where   PO.ID               = ?\n" +
                "        and     AE.LINK_MARKER     = ?\n" +
                "        order   by AE.IDX_NO",hFRPO_PO_OBJ8
                , hFRPO_ACTIONS_ENTITY_d_theTableMarker);


        Response iRc=returnOK();
        iRc.setBody(FRPO_ACTIONS_ENT);

        return iRc;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param frpo_actions_entity
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 16:50
     */
    public Response fetchFRPO_ACTIONS_ENTITY(Infos.FrpoActionsEntity frpo_actions_entity,Object[] FRPO_ACTIONS_ENT) {
        String hFRPO_ACTIONS_ENTITY_CLASS_NAME;
        String hFRPO_ACTIONS_ENTITY_ENTITY_ID;
        String hFRPO_ACTIONS_ENTITY_ENTITY_ATTRIB;


        hFRPO_ACTIONS_ENTITY_CLASS_NAME=convert(FRPO_ACTIONS_ENT[0]);
        hFRPO_ACTIONS_ENTITY_ENTITY_ID=convert(FRPO_ACTIONS_ENT[1]);
        hFRPO_ACTIONS_ENTITY_ENTITY_ATTRIB=convert(FRPO_ACTIONS_ENT[2]);



        frpo_actions_entity.setClass_name(hFRPO_ACTIONS_ENTITY_CLASS_NAME);
        frpo_actions_entity.setEntity_id(hFRPO_ACTIONS_ENTITY_ENTITY_ID);
        frpo_actions_entity.setEntity_attrib(hFRPO_ACTIONS_ENTITY_ENTITY_ATTRIB);

        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataActionEntityRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 16:56
     */
    public Response insertCollectedDataActionEntityHistory(Infos.OhcdatahsActionEntity collectedDataActionEntityRecord) {
        String hFHCDATAHS_ACTIONS_ENTITY_MEAS_LOT_ID       ;
        String hFHCDATAHS_ACTIONS_ENTITY_MEAS_MAINPD_ID    ;
        String hFHCDATAHS_ACTIONS_ENTITY_MEAS_OPE_NO       ;
        Integer hFHCDATAHS_ACTIONS_ENTITY_MEAS_PASS_COUNT;
        String hFHCDATAHS_ACTIONS_ENTITY_CLAIM_TIME        ;
        String hFHCDATAHS_ACTIONS_ENTITY_MEAS_DCDEF_ID     ;
        String hFHCDATAHS_ACTIONS_ENTITY_CHECK_TYPE        ;
        String hFHCDATAHS_ACTIONS_ENTITY_REASON_CODE       ;
        String hFHCDATAHS_ACTIONS_ENTITY_ACT_CODE          ;
        String hFHCDATAHS_ACTIONS_ENTITY_PROC_LOT_ID       ;
        String hFHCDATAHS_ACTIONS_ENTITY_PROC_MAINPD_ID    ;
        String hFHCDATAHS_ACTIONS_ENTITY_PROC_OPE_NO       ;
        Integer hFHCDATAHS_ACTIONS_ENTITY_PROC_PASS_COUNT;
        String hFHCDATAHS_ACTIONS_ENTITY_CLASS_NAME        ;
        String hFHCDATAHS_ACTIONS_ENTITY_ENTITY_ID         ;
        String hFHCDATAHS_ACTIONS_ENTITY_ENTITY_ATTRIB     ;


        hFHCDATAHS_ACTIONS_ENTITY_MEAS_LOT_ID = collectedDataActionEntityRecord.getMeas_lot_id ();
        hFHCDATAHS_ACTIONS_ENTITY_MEAS_MAINPD_ID = collectedDataActionEntityRecord.getMeas_mainpd_id ();
        hFHCDATAHS_ACTIONS_ENTITY_MEAS_OPE_NO = collectedDataActionEntityRecord.getMeas_ope_no ();
        hFHCDATAHS_ACTIONS_ENTITY_MEAS_PASS_COUNT  = collectedDataActionEntityRecord.getMeas_pass_count();
        hFHCDATAHS_ACTIONS_ENTITY_CLAIM_TIME = collectedDataActionEntityRecord.getClaim_time ();
        hFHCDATAHS_ACTIONS_ENTITY_MEAS_DCDEF_ID = collectedDataActionEntityRecord.getMeas_dcdef_id ();
        hFHCDATAHS_ACTIONS_ENTITY_CHECK_TYPE = collectedDataActionEntityRecord.getCheck_type ();
        hFHCDATAHS_ACTIONS_ENTITY_REASON_CODE = collectedDataActionEntityRecord.getReason_code ();
        hFHCDATAHS_ACTIONS_ENTITY_ACT_CODE = collectedDataActionEntityRecord.getAct_code ();
        hFHCDATAHS_ACTIONS_ENTITY_PROC_LOT_ID = collectedDataActionEntityRecord.getProc_lot_id ();
        hFHCDATAHS_ACTIONS_ENTITY_PROC_MAINPD_ID = collectedDataActionEntityRecord.getProc_mainpd_id ();
        hFHCDATAHS_ACTIONS_ENTITY_PROC_OPE_NO = collectedDataActionEntityRecord.getProc_ope_no ();
        hFHCDATAHS_ACTIONS_ENTITY_PROC_PASS_COUNT  = collectedDataActionEntityRecord.getProc_pass_count();
        hFHCDATAHS_ACTIONS_ENTITY_CLASS_NAME = collectedDataActionEntityRecord.getClass_name ();
        hFHCDATAHS_ACTIONS_ENTITY_ENTITY_ID = collectedDataActionEntityRecord.getEntity_id ();
        hFHCDATAHS_ACTIONS_ENTITY_ENTITY_ATTRIB = collectedDataActionEntityRecord.getEntity_attrib ();

        baseCore.insert("INSERT INTO OHEDC_ACT_RESTRICT\n" +
                "                (  ID,MEAS_LOT_ID,\n" +
                "                        MEAS_PROCESS_ID,\n" +
                "                        MEAS_OPE_NO,\n" +
                "                        MEAS_PASS_COUNT,\n" +
                "                        TRX_TIME,\n" +
                "                        MEAS_EDC_PLAN_ID,\n" +
                "                        CHECK_TYPE,\n" +
                "                        REASON_CODE,\n" +
                "                        ACTION_CODE,\n" +
                "                        PROC_LOT_ID,\n" +
                "                        PROC_PROCESS_ID,\n" +
                "                        PROC_OPE_NO,\n" +
                "                        PROC_PASS_COUNT,\n" +
                "                        CLASS_NAME,\n" +
                "                        CLASS_ID,\n" +
                "                        CLASS_ATTRIB,\n" +
                "                        STORE_TIME )\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                CURRENT_TIMESTAMP )",generateID(Infos.OhcdatahsActionEntity.class)
                ,hFHCDATAHS_ACTIONS_ENTITY_MEAS_LOT_ID
                , hFHCDATAHS_ACTIONS_ENTITY_MEAS_MAINPD_ID
                , hFHCDATAHS_ACTIONS_ENTITY_MEAS_OPE_NO
                , hFHCDATAHS_ACTIONS_ENTITY_MEAS_PASS_COUNT
                , convert(hFHCDATAHS_ACTIONS_ENTITY_CLAIM_TIME)
                , hFHCDATAHS_ACTIONS_ENTITY_MEAS_DCDEF_ID
                , hFHCDATAHS_ACTIONS_ENTITY_CHECK_TYPE
                , hFHCDATAHS_ACTIONS_ENTITY_REASON_CODE
                , hFHCDATAHS_ACTIONS_ENTITY_ACT_CODE
                , hFHCDATAHS_ACTIONS_ENTITY_PROC_LOT_ID
                , hFHCDATAHS_ACTIONS_ENTITY_PROC_MAINPD_ID
                , hFHCDATAHS_ACTIONS_ENTITY_PROC_OPE_NO
                , hFHCDATAHS_ACTIONS_ENTITY_PROC_PASS_COUNT
                , hFHCDATAHS_ACTIONS_ENTITY_CLASS_NAME
                , hFHCDATAHS_ACTIONS_ENTITY_ENTITY_ID
                , hFHCDATAHS_ACTIONS_ENTITY_ENTITY_ATTRIB);



        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 16:05
     */
    public Response createFHCDATAHS_ACTIONS(Infos.CollectedDataEventRecord collectedDataEventRecord,
                             List<Infos.UserDataSet> userDataSets ) {
        Infos.OhcdatahsAction fhcdatahs_action=new Infos.OhcdatahsAction();
        Infos.OhcdatahsActionEntity fhcdatahs_action_entity=new Infos.OhcdatahsActionEntity();

        Response                         iRc = returnOK();
        Params.Param<Integer> seqNo=new Params.Param<>();
        Infos.FrpoActions poAction=new Infos.FrpoActions();
        Infos.FrpoActionsEntity poActionEntity=new Infos.FrpoActionsEntity();

        iRc = selectFRPO_ACTIONS( collectedDataEventRecord.getMeasuredLotData().getObjrefPO() );
        if ( !isOk(iRc ) ) {
            return iRc;
        }

        List<Object[]> FRPO_ACTIONS=convert(iRc.getBody());

        for ( Object[] FRPO_ACTION:FRPO_ACTIONS) {
            poAction=new Infos.FrpoActions();
            iRc = fetchFRPO_ACTIONS( seqNo, poAction, FRPO_ACTION);

            if ( !isOk(iRc ) ) {
                return iRc;
            }

            fhcdatahs_action=new Infos.OhcdatahsAction();
            fhcdatahs_action.setMeas_lot_id(collectedDataEventRecord.getMeasuredLotData().getLotID ());
            fhcdatahs_action.setMeas_mainpd_id(collectedDataEventRecord.getMeasuredLotData().getRouteID ());
            fhcdatahs_action.setMeas_ope_no(collectedDataEventRecord.getMeasuredLotData().getOperationNumber ());
            fhcdatahs_action.setMeas_pass_count (collectedDataEventRecord.getMeasuredLotData().getOperationPassCount()) ;
            fhcdatahs_action.setClaim_time(collectedDataEventRecord.getEventCommon().getEventTimeStamp ());
            fhcdatahs_action.setProc_lot_id(poAction.getLot_id ());
            fhcdatahs_action.setMlot_flg        (poAction.getMlot_flg());
            fhcdatahs_action.setMeas_dcdef_id(poAction.getDcdef_id ());
            fhcdatahs_action.setMeas_dcspec_id(poAction.getDcspec_id ());
            fhcdatahs_action.setCheck_type(poAction.getCheck_type ());
            fhcdatahs_action.setReason_code(poAction.getReason_code ());
            fhcdatahs_action.setAct_code(poAction.getAct_code ());
            fhcdatahs_action.setProc_mainpd_id(poAction.getProc_mainpd_id ());
            fhcdatahs_action.setProc_ope_no(poAction.getProc_ope_no ());
            fhcdatahs_action.setProc_pd_id(poAction.getProc_pd_id ());
            fhcdatahs_action.setProc_pass_count (poAction.getProc_pass_count());
            fhcdatahs_action.setProc_eqp_id(poAction.getProc_eqp_id ());
            fhcdatahs_action.setProc_recipe_id(poAction.getProc_recipe_id ());
            fhcdatahs_action.setProc_fab_id(poAction.getProc_fab_id ());
            fhcdatahs_action.setBank_id(poAction.getBank_id ());
            fhcdatahs_action.setRework_route_id(poAction.getRework_route_id ());

            iRc = insertCollectedDataActionHistory( fhcdatahs_action );
            if ( !isOk(iRc ) ) {
                return iRc;
            }

            iRc = selectFRPO_ACTIONS_ENTITY( collectedDataEventRecord.getMeasuredLotData().getObjrefPO(), seqNo );
            if ( !isOk(iRc ) ) {
                return iRc;
            }

            List<Object[]> FRPO_ACTIONS_ENTS= convert(iRc.getBody());

            for (Object[] FRPO_ACTIONS_ENT: FRPO_ACTIONS_ENTS ) {
                poActionEntity=new Infos.FrpoActionsEntity();
                iRc = fetchFRPO_ACTIONS_ENTITY( poActionEntity,FRPO_ACTIONS_ENT );
                if ( !isOk(iRc ) ) {
                    return iRc;
                }

                fhcdatahs_action_entity=new Infos.OhcdatahsActionEntity();
                fhcdatahs_action_entity.setMeas_lot_id(collectedDataEventRecord.getMeasuredLotData().getLotID ());
                fhcdatahs_action_entity.setMeas_mainpd_id(collectedDataEventRecord.getMeasuredLotData().getRouteID ());
                fhcdatahs_action_entity.setMeas_ope_no(collectedDataEventRecord.getMeasuredLotData().getOperationNumber ());
                fhcdatahs_action_entity.setMeas_pass_count (collectedDataEventRecord.getMeasuredLotData().getOperationPassCount ());
                fhcdatahs_action_entity.setClaim_time(collectedDataEventRecord.getEventCommon().getEventTimeStamp ());
                fhcdatahs_action_entity.setProc_lot_id(poAction.getLot_id ());
                fhcdatahs_action_entity.setMeas_dcdef_id(poAction.getDcdef_id ());
                fhcdatahs_action_entity.setCheck_type(poAction.getCheck_type ());
                fhcdatahs_action_entity.setReason_code(poAction.getReason_code ());
                fhcdatahs_action_entity.setAct_code(poAction.getAct_code ());
                fhcdatahs_action_entity.setProc_mainpd_id(poAction.getProc_mainpd_id ());
                fhcdatahs_action_entity.setProc_ope_no(poAction.getProc_ope_no ());
                fhcdatahs_action_entity.setProc_pass_count (poAction.getProc_pass_count());
                fhcdatahs_action_entity.setClass_name(poActionEntity.getClass_name ());
                fhcdatahs_action_entity.setEntity_id(poActionEntity.getEntity_id ());
                fhcdatahs_action_entity.setEntity_attrib(poActionEntity.getEntity_attrib ());

                iRc = insertCollectedDataActionEntityHistory( fhcdatahs_action_entity );
                if ( !isOk(iRc ) ) {
                    return iRc;
                }
            }

        }

        return returnOK();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param poObj
     * @param count
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 17:43
     */
    public Response countFRPO_DC(String poObj, Params.Param<Integer > count) {
        Integer hFRPO_DC_DELTA_COUNT2  ;
        String hFRPO_PO_OBJ4;

        hFRPO_PO_OBJ4         ="";
        hFRPO_DC_DELTA_COUNT2    = 0;
        count.setT(0);

        hFRPO_PO_OBJ4        = poObj       ;

        int one=baseCore.count("SELECT  COUNT(PO_DC.REFKEY) \n" +
                "        FROM OMPROPE_EDC  PO_DC,\n" +
                "                OMPROPE     PO\n" +
                "        Where   PO.ID             = ?\n" +
                "        AND     PO_DC.REFKEY  = PO.ID",hFRPO_PO_OBJ4);

        hFRPO_DC_DELTA_COUNT2=one;

        if( hFRPO_DC_DELTA_COUNT2 <= 0 ) {
            count .setT(0);
        } else {
            count .setT(hFRPO_DC_DELTA_COUNT2 );
        }

        Response iRc=returnOK();

        return(iRc);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param poObj
     * @param dcdef_id
     * @param dcdef_desc
     * @param dc_type
     * @param dcspec_id
     * @param dcspec_desc
     * @param d_SeqNo
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 17:49
     */
    public Response getFRPO_DC(String poObj, Params.String dcdef_id, Params.String dcdef_desc, Params.String dc_type,
                               Params.String dcspec_id, Params.String dcspec_desc, Params.Param<Integer> d_SeqNo ) {
        String hFRPO_DC_DCDEF_ID    ;
        String hFRPO_DC_DC_TYPE     ;
        String hFRPO_DC_DCSPEC_ID   ;
        String hFRPO_DC_DCDEF_DESC ;
        String hFRPO_DC_DCSPEC_DESC;
        Integer hFRPO_DC_d_SeqNo;
        Integer hFRPO_DC_d_SeqNo_Temp;
        String hFRPO_PO_OBJ3;

        dcdef_id.setValue("");
        dc_type.setValue("");
        dcspec_id.setValue("");
        dcdef_desc.setValue("");
        dcspec_desc.setValue("");

        hFRPO_DC_d_SeqNo_Temp  = 0 ;

        hFRPO_DC_d_SeqNo = 0;
        hFRPO_DC_DCDEF_ID="";
        hFRPO_DC_DC_TYPE="";
        hFRPO_DC_DCSPEC_ID="";
        hFRPO_DC_DCDEF_DESC="";
        hFRPO_DC_DCSPEC_DESC="";
        hFRPO_PO_OBJ3="";

        hFRPO_PO_OBJ3 = poObj       ;
        hFRPO_DC_d_SeqNo_Temp  = d_SeqNo.getT() ;


        Object[] one=baseCore.queryOne("SELECT  PO_DC.EDC_PLAN_ID,       PO_DC.EDC_TYPE,        PO_DC.EDC_SPEC_ID,     PO_DC.IDX_NO,\n" +
                "                PO_DC.DESCRIPTION,    PO_DC.EDC_SPEC_DESC\n" +
                "        FROM    OMPROPE_EDC  PO_DC,\n" +
                "                OMPROPE     PO\n" +
                "        WHERE   PO.ID             = ?\n" +
                "        AND     PO_DC.IDX_NO         = ?\n" +
                "        AND     PO_DC.REFKEY  = PO.ID",hFRPO_PO_OBJ3
                , hFRPO_DC_d_SeqNo_Temp);

        hFRPO_DC_DCDEF_ID = convert(one[0]);
                hFRPO_DC_DC_TYPE = convert(one[1]);
        hFRPO_DC_DCSPEC_ID = convert(one[2]);
                hFRPO_DC_d_SeqNo = convertI(one[3]);
        hFRPO_DC_DCDEF_DESC = convert(one[4]);
                hFRPO_DC_DCSPEC_DESC = convert(one[5]);


        dcdef_id .setValue(hFRPO_DC_DCDEF_ID);
        dc_type .setValue(hFRPO_DC_DC_TYPE);
        dcspec_id .setValue(hFRPO_DC_DCSPEC_ID);
        dcdef_desc .setValue(hFRPO_DC_DCDEF_DESC);
        dcspec_desc .setValue(hFRPO_DC_DCSPEC_DESC);
        d_SeqNo .setT(hFRPO_DC_d_SeqNo );

        Response iRc=returnOK();
        return(iRc);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param poObj
     * @param d_SeqNo
     * @param count
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 17:55
     */
    public Response selectFRPO_DC_SPECS(String poObj, Params.Param<Integer> d_SeqNo, Params.Param<Integer> count ) {
        String hFRPO_DC_SPECS_d_theTableMarker;
        Integer hFRPO_DC_SPECS_COUNT;
        String hFRPO_PO_OBJ6;


        count.setValue(0);
        hFRPO_DC_SPECS_COUNT = 0;
        hFRPO_DC_SPECS_d_theTableMarker="";
        hFRPO_PO_OBJ6="";

        hFRPO_DC_SPECS_d_theTableMarker=String.format("%d", d_SeqNo.getValue() );
        hFRPO_PO_OBJ6=poObj ;


        int one=baseCore.count("SELECT count(DC_SPECS.REFKEY) \n" +
                "        FROM   OMPROPE_EDC_SPECS DC_SPECS,\n" +
                "                OMPROPE          PO\n" +
                "        WHERE  PO.ID                 = ?\n" +
                "        AND    PO.ID         = DC_SPECS.REFKEY\n" +
                "        AND    DC_SPECS.LINK_MARKER = ?",hFRPO_PO_OBJ6
                , hFRPO_DC_SPECS_d_theTableMarker);
        hFRPO_DC_SPECS_COUNT=one;

        count.setValue( hFRPO_DC_SPECS_COUNT);

        if ( count.getValue() == 0 ) {
            return( returnOK());
        }

        List<Object[]> DC_2=baseCore.queryAll("SELECT  DC_SPECS.EDC_ITEM_NAME,\n" +
                "                DC_SPECS.SCRN_UP_FLAG,  DC_SPECS.SCRN_UP_LIMIT,  DC_SPECS.SCRN_UP_ACT,\n" +
                "                DC_SPECS.SCRN_LO_FLAG,  DC_SPECS.SCRN_LO_LIMIT,  DC_SPECS.SCRN_LO_ACT,\n" +
                "                DC_SPECS.SPEC_UP_FLAG,  DC_SPECS.SPEC_UP_LIMIT,  DC_SPECS.SPEC_UP_ACT,\n" +
                "                DC_SPECS.SPEC_LO_FLAG,  DC_SPECS.SPEC_LO_LIMIT,  DC_SPECS.SPEC_LO_ACT,\n" +
                "                DC_SPECS.CTRL_UP_FLAG,  DC_SPECS.CTRL_UP_LIMIT,  DC_SPECS.CTRL_UP_ACT,\n" +
                "                DC_SPECS.CTRL_LO_FLAG,  DC_SPECS.CTRL_LO_LIMIT,  DC_SPECS.CTRL_LO_ACT,\n" +
                "        DC_SPECS.EDC_ITEM_TARGET,   DC_SPECS.EDC_ITEM_TAG,        DC_SPECS.EDC_SPEC_GROUP   \n" +
                "        FROM    OMPROPE_EDC_SPECS             DC_SPECS,\n" +
                "                OMPROPE                      PO\n" +
                "        WHERE   PO.ID                 = ?\n" +
                "        AND     PO.ID         = DC_SPECS.REFKEY\n" +
                "        AND     DC_SPECS.LINK_MARKER = ?",hFRPO_PO_OBJ6
                , hFRPO_DC_SPECS_d_theTableMarker);


        Response iRc=returnOK();

        iRc.setBody(DC_2);

        return( iRc );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataSpecRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 18:29
     */
    public Response insertCollectedDataSpecHistory(Infos.OhcdatahsSpec collectedDataSpecRecord) {
        String hFHCDATAHS_SPEC_MEAS_LOT_ID        ;
        String hFHCDATAHS_SPEC_MEAS_MAINPD_ID     ;
        String hFHCDATAHS_SPEC_MEAS_OPE_NO        ;
        Integer hFHCDATAHS_SPEC_MEAS_PASS_COUNT         ;
        String hFHCDATAHS_SPEC_CLAIM_TIME         ;
        String hFHCDATAHS_SPEC_MEAS_DCDEF_ID      ;
        String hFHCDATAHS_SPEC_MEAS_DCDEF_DESC    ;
        String hFHCDATAHS_SPEC_MEAS_DC_TYPE       ;
        String hFHCDATAHS_SPEC_MEAS_DCSPEC_ID     ;
        String hFHCDATAHS_SPEC_MEAS_DCSPEC_DESC   ;
        String hFHCDATAHS_SPEC_DCITEM_NAME        ;
        Integer hFHCDATAHS_SPEC_SCRN_UPPER_REQ          ;
        Double  hFHCDATAHS_SPEC_SCRN_UPPER_LIMIT        ;
        String hFHCDATAHS_SPEC_SCRN_UPPER_ACTIONS;
        Integer hFHCDATAHS_SPEC_SPEC_UPPER_REQ          ;
        Double  hFHCDATAHS_SPEC_SPEC_UPPER_LIMIT        ;
        String hFHCDATAHS_SPEC_SPEC_UPPER_ACTIONS;
        Integer hFHCDATAHS_SPEC_CNTL_UPPER_REQ          ;
        Double  hFHCDATAHS_SPEC_CNTL_UPPER_LIMIT        ;
        String hFHCDATAHS_SPEC_CNTL_UPPER_ACTIONS;
        Integer hFHCDATAHS_SPEC_CNTL_LOWER_REQ          ;
        Double  hFHCDATAHS_SPEC_CNTL_LOWER_LIMIT        ;
        String hFHCDATAHS_SPEC_CNTL_LOWER_ACTIONS;
        Integer hFHCDATAHS_SPEC_SPEC_LOWER_REQ          ;
        Double  hFHCDATAHS_SPEC_SPEC_LOWER_LIMIT        ;
        String hFHCDATAHS_SPEC_SPEC_LOWER_ACTIONS;
        Integer hFHCDATAHS_SPEC_SCRN_LOWER_REQ          ;
        Double  hFHCDATAHS_SPEC_SCRN_LOWER_LIMIT        ;
        String hFHCDATAHS_SPEC_SCRN_LOWER_ACTIONS;
        Double  hFHCDATAHS_SPEC_DCITEM_TARGET           ;
        String hFHCDATAHS_SPEC_DCITEM_TAG        ;
        String hFHCDATAHS_SPEC_DC_SPEC_GROUP      ;


        hFHCDATAHS_SPEC_MEAS_LOT_ID        = collectedDataSpecRecord.getMeas_lot_id        ();
        hFHCDATAHS_SPEC_MEAS_MAINPD_ID     = collectedDataSpecRecord.getMeas_mainpd_id     ();
        hFHCDATAHS_SPEC_MEAS_OPE_NO        = collectedDataSpecRecord.getMeas_ope_no        ();
        hFHCDATAHS_SPEC_MEAS_PASS_COUNT    =  collectedDataSpecRecord.getMeas_pass_count     ();
        hFHCDATAHS_SPEC_CLAIM_TIME         = collectedDataSpecRecord.getClaim_time         ();
        hFHCDATAHS_SPEC_MEAS_DCDEF_ID      = collectedDataSpecRecord.getMeas_dcdef_id      ();
        hFHCDATAHS_SPEC_MEAS_DCDEF_DESC    = collectedDataSpecRecord.getMeas_dcdef_desc    ();
        hFHCDATAHS_SPEC_MEAS_DC_TYPE       = collectedDataSpecRecord.getMeas_dc_type       ();
        hFHCDATAHS_SPEC_MEAS_DCSPEC_ID     = collectedDataSpecRecord.getMeas_dcspec_id     ();
        hFHCDATAHS_SPEC_MEAS_DCSPEC_DESC   = collectedDataSpecRecord.getMeas_dcspec_desc   ();
        hFHCDATAHS_SPEC_DCITEM_NAME        = collectedDataSpecRecord.getDcitem_name        ();
        hFHCDATAHS_SPEC_SCRN_UPPER_REQ     =  collectedDataSpecRecord.getScrn_upper_req      ();
        hFHCDATAHS_SPEC_SCRN_UPPER_LIMIT   =  collectedDataSpecRecord.getScrn_upper_limit    ();
        hFHCDATAHS_SPEC_SCRN_UPPER_ACTIONS = collectedDataSpecRecord.getScrn_upper_actions ();
        hFHCDATAHS_SPEC_SPEC_UPPER_REQ     =  collectedDataSpecRecord.getSpec_upper_req      ();
        hFHCDATAHS_SPEC_SPEC_UPPER_LIMIT   =  collectedDataSpecRecord.getSpec_upper_limit    ();
        hFHCDATAHS_SPEC_SPEC_UPPER_ACTIONS = collectedDataSpecRecord.getSpec_upper_actions ();
        hFHCDATAHS_SPEC_CNTL_UPPER_REQ     =  collectedDataSpecRecord.getCntl_upper_req      ();
        hFHCDATAHS_SPEC_CNTL_UPPER_LIMIT   =  collectedDataSpecRecord.getCntl_upper_limit    ();
        hFHCDATAHS_SPEC_CNTL_UPPER_ACTIONS = collectedDataSpecRecord.getCntl_upper_actions ();
        hFHCDATAHS_SPEC_CNTL_LOWER_REQ     =  collectedDataSpecRecord.getCntl_lower_req      ();
        hFHCDATAHS_SPEC_CNTL_LOWER_LIMIT   =  collectedDataSpecRecord.getCntl_lower_limit    ();
        hFHCDATAHS_SPEC_CNTL_LOWER_ACTIONS = collectedDataSpecRecord.getCntl_lower_actions ();
        hFHCDATAHS_SPEC_SPEC_LOWER_REQ     =  collectedDataSpecRecord.getSpec_lower_req      ();
        hFHCDATAHS_SPEC_SPEC_LOWER_LIMIT   =  collectedDataSpecRecord.getSpec_lower_limit    ();
        hFHCDATAHS_SPEC_SPEC_LOWER_ACTIONS = collectedDataSpecRecord.getSpec_lower_actions ();
        hFHCDATAHS_SPEC_SCRN_LOWER_REQ     =  collectedDataSpecRecord.getScrn_lower_req      ();
        hFHCDATAHS_SPEC_SCRN_LOWER_LIMIT   =  collectedDataSpecRecord.getScrn_lower_limit    ();
        hFHCDATAHS_SPEC_SCRN_LOWER_ACTIONS = collectedDataSpecRecord.getScrn_lower_actions ();
        hFHCDATAHS_SPEC_DCITEM_TARGET      =  collectedDataSpecRecord.getDcitem_target       ();
        hFHCDATAHS_SPEC_DCITEM_TAG         = collectedDataSpecRecord.getDcitem_tag         ();
        hFHCDATAHS_SPEC_DC_SPEC_GROUP      = collectedDataSpecRecord.getDcspec_group       ();

        baseCore.insert("INSERT INTO OHEDC_SPEC\n" +
                "                (  ID,MEAS_LOT_ID       ,\n" +
                "                        MEAS_PROCESS_ID    ,\n" +
                "                        MEAS_OPE_NO       ,\n" +
                "                        MEAS_PASS_COUNT   ,\n" +
                "                        TRX_TIME        ,\n" +
                "                        MEAS_EDC_PLAN_ID     ,\n" +
                "                        MEAS_EDC_PLAN_DESC   ,\n" +
                "                        MEAS_EDC_TYPE      ,\n" +
                "                        MEAS_EDC_SPEC_ID    ,\n" +
                "                        MEAS_EDC_SPEC_DESC  ,\n" +
                "                        EDC_ITEM_NAME       ,\n" +
                "                        SCRN_UP_FLAG    ,\n" +
                "                        SCRN_UP_LIMIT  ,\n" +
                "                        SCRN_UP_ACT,\n" +
                "                        SPEC_UP_FLAG    ,\n" +
                "                        SPEC_UP_LIMIT  ,\n" +
                "                        SPEC_UP_ACT,\n" +
                "                        CTRL_UP_FLAG    ,\n" +
                "                        CTRL_UP_LIMIT  ,\n" +
                "                        CTRL_UP_ACT,\n" +
                "                        CTRL_LO_FLAG    ,\n" +
                "                        CTRL_LO_LIMIT  ,\n" +
                "                        CTRL_LO_ACT,\n" +
                "                        SPEC_LO_FLAG    ,\n" +
                "                        SPEC_LO_LIMIT  ,\n" +
                "                        SPEC_LO_ACT,\n" +
                "                        SCRN_LO_FLAG    ,\n" +
                "                        SCRN_LO_LIMIT  ,\n" +
                "                        SCRN_LO_ACT,\n" +
                "                        EDC_ITEM_TARGET     ,\n" +
                "                        EDC_ITEM_TAG        ,\n" +
                "                        EDC_SPEC_GROUP     ,   \n" +
                "                        STORE_TIME )\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,  \n" +
                "        CURRENT_TIMESTAMP                  )",generateID(Infos.OhcdatahsSpec.class)
                ,hFHCDATAHS_SPEC_MEAS_LOT_ID
                , hFHCDATAHS_SPEC_MEAS_MAINPD_ID
                , hFHCDATAHS_SPEC_MEAS_OPE_NO
                , hFHCDATAHS_SPEC_MEAS_PASS_COUNT
                , convert(hFHCDATAHS_SPEC_CLAIM_TIME)
                , hFHCDATAHS_SPEC_MEAS_DCDEF_ID
                , hFHCDATAHS_SPEC_MEAS_DCDEF_DESC
                , hFHCDATAHS_SPEC_MEAS_DC_TYPE
                , hFHCDATAHS_SPEC_MEAS_DCSPEC_ID
                , hFHCDATAHS_SPEC_MEAS_DCSPEC_DESC
                , hFHCDATAHS_SPEC_DCITEM_NAME
                , hFHCDATAHS_SPEC_SCRN_UPPER_REQ
                , hFHCDATAHS_SPEC_SCRN_UPPER_LIMIT
                , hFHCDATAHS_SPEC_SCRN_UPPER_ACTIONS
                , hFHCDATAHS_SPEC_SPEC_UPPER_REQ
                , hFHCDATAHS_SPEC_SPEC_UPPER_LIMIT
                , hFHCDATAHS_SPEC_SPEC_UPPER_ACTIONS
                , hFHCDATAHS_SPEC_CNTL_UPPER_REQ
                , hFHCDATAHS_SPEC_CNTL_UPPER_LIMIT
                , hFHCDATAHS_SPEC_CNTL_UPPER_ACTIONS
                , hFHCDATAHS_SPEC_CNTL_LOWER_REQ
                , hFHCDATAHS_SPEC_CNTL_LOWER_LIMIT
                , hFHCDATAHS_SPEC_CNTL_LOWER_ACTIONS
                , hFHCDATAHS_SPEC_SPEC_LOWER_REQ
                , hFHCDATAHS_SPEC_SPEC_LOWER_LIMIT
                , hFHCDATAHS_SPEC_SPEC_LOWER_ACTIONS
                , hFHCDATAHS_SPEC_SCRN_LOWER_REQ
                , hFHCDATAHS_SPEC_SCRN_LOWER_LIMIT
                , hFHCDATAHS_SPEC_SCRN_LOWER_ACTIONS
                , hFHCDATAHS_SPEC_DCITEM_TARGET
                , hFHCDATAHS_SPEC_DCITEM_TAG
                , hFHCDATAHS_SPEC_DC_SPEC_GROUP     );


        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 16:03
     */
    public Response createFHCDATAHS_SPEC(Infos.CollectedDataEventRecord collectedDataEventRecord,  List<Infos.UserDataSet> userDataSets ) {
        Infos.OhcdatahsSpec fhcdatahs_spec=null;

        Response                     iRc = returnOK();
        int            i = 0, j = 0;
        Params.Param<Integer> d_SeqNO = new Params.Param<>(0);
        Params.Param<Integer> rec_count = new Params.Param<>(0);
        Params.Param<Integer> count = new Params.Param<>(0);

        Params.String dcdefID=new Params.String();
        Params.String dcdefDesc=new Params.String();
        Params.String dc_type=new Params.String();
        Params.String dcspecID=new Params.String();
        Params.String dcspecDesc=new Params.String();
        Params.String dcItemName=new Params.String();
        Params.String screenUpperActions=new Params.String();
        Params.String screenLowerActions=new Params.String();
        Params.String specUpperActions=new Params.String();
        Params.String specLowerActions=new Params.String();
        Params.String controlUpperActions=new Params.String();
        Params.String controlLowerActions=new Params.String();
        Params.String dcItemTag=new Params.String();
        Params.String dcSpecGroup=new Params.String();
        Params.Param<Integer> screenUpperReq = new Params.Param<>(0);
        Params.Param<Integer> screenLowerReq = new Params.Param<>(0);
        Params.Param<Integer> specUpperReq = new Params.Param<>(0);
        Params.Param<Integer> specLowerReq = new Params.Param<>(0);
        Params.Param<Integer> controlUpperReq = new Params.Param<>(0);
        Params.Param<Integer> controlLowerReq = new Params.Param<>(0);
        Params.Param<Double> screenUpperLimit = new Params.Param<>(0.0);
        Params.Param<Double> screenLowerLimit = new Params.Param<>(0.0);
        Params.Param<Double> specUpperLimit = new Params.Param<>(0.0);
        Params.Param<Double> specLowerLimit = new Params.Param<>(0.0);
        Params.Param<Double> controlUpperLimit = new Params.Param<>(0.0);
        Params.Param<Double> controlLowerLimit = new Params.Param<>(0.0);
        Params.Param<Double> dcItemTarget = new Params.Param<>(0.0);


        iRc = countFRPO_DC( collectedDataEventRecord.getMeasuredLotData().getObjrefPO(),
                rec_count );
        if( !isOk(iRc) ) {
            return( iRc );
        }

        for( i = 0; i < rec_count.getValue() ; i++ ) {
            d_SeqNO .setValue(i);

            iRc = getFRPO_DC( collectedDataEventRecord.getMeasuredLotData().getObjrefPO(),
                    dcdefID,
                    dcdefDesc,
                    dc_type,
                    dcspecID,
                    dcspecDesc,
                    d_SeqNO );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            if( strlen(dcspecID.getValue()) != 0 ) {
                count.setValue(0);
                iRc = selectFRPO_DC_SPECS( collectedDataEventRecord.getMeasuredLotData().getObjrefPO(),
                        d_SeqNO,
                        count );
                if( !isOk(iRc) ) {
                    return( iRc );
                }
                List<Object[]> DC_2=convert(iRc.getBody());

                for( j = 0; j < count.getValue(); j++ ) {

                    // fetchFRPO_DC_SPECS
                    String  hFRPO_DC_SPECS_DCITEM_NAME;
                    Integer hFRPO_DC_SPECS_SCRN_UPPER_REQ;
                    Double hFRPO_DC_SPECS_SCRN_UPPER_LIMIT;
                    String  hFRPO_DC_SPECS_SCRN_UPPER_ACTIONS;
                    Integer hFRPO_DC_SPECS_SCRN_LOWER_REQ;
                    Double hFRPO_DC_SPECS_SCRN_LOWER_LIMIT;
                    String  hFRPO_DC_SPECS_SCRN_LOWER_ACTIONS;
                    Integer hFRPO_DC_SPECS_SPEC_UPPER_REQ;
                    Double hFRPO_DC_SPECS_SPEC_UPPER_LIMIT;
                    String  hFRPO_DC_SPECS_SPEC_UPPER_ACTIONS;
                    Integer hFRPO_DC_SPECS_SPEC_LOWER_REQ;
                    Double hFRPO_DC_SPECS_SPEC_LOWER_LIMIT;
                    String  hFRPO_DC_SPECS_SPEC_LOWER_ACTIONS;
                    Integer hFRPO_DC_SPECS_CNTL_UPPER_REQ;
                    Double hFRPO_DC_SPECS_CNTL_UPPER_LIMIT;
                    String  hFRPO_DC_SPECS_CNTL_UPPER_ACTIONS;
                    Integer hFRPO_DC_SPECS_CNTL_LOWER_REQ;
                    Double hFRPO_DC_SPECS_CNTL_LOWER_LIMIT;
                    String  hFRPO_DC_SPECS_CNTL_LOWER_ACTIONS;
                    Double hFRPO_DC_SPECS_DCITEM_TARGET;
                    String  hFRPO_DC_SPECS_DCITEM_TAG;
                    String  hFRPO_DC_SPECS_DC_SPEC_GROUP;


                    dcItemName.setValue("");
                    screenUpperActions.setValue("");
                    screenLowerActions.setValue("");
                    specUpperActions.setValue("");
                    specLowerActions.setValue("");
                    controlUpperActions.setValue("");
                    controlLowerActions.setValue("");
                    dcItemTag.setValue("");
                    dcSpecGroup.setValue("");
                    screenUpperReq    .setValue(0);
                    screenLowerReq    .setValue(0);
                    specUpperReq      .setValue(0);
                    specLowerReq      .setValue(0);
                    controlUpperReq   .setValue(0);
                    controlLowerReq   .setValue(0);
                    screenUpperLimit  .setValue(0.0);
                    screenLowerLimit  .setValue(0.0);
                    specUpperLimit    .setValue(0.0);
                    specLowerLimit    .setValue(0.0);
                    controlUpperLimit .setValue(0.0);
                    controlLowerLimit .setValue(0.0);
                    dcItemTarget      .setValue(0.0);

                    hFRPO_DC_SPECS_DCITEM_NAME="";
                    hFRPO_DC_SPECS_SCRN_UPPER_ACTIONS="";
                    hFRPO_DC_SPECS_SCRN_LOWER_ACTIONS="";
                    hFRPO_DC_SPECS_SPEC_UPPER_ACTIONS="";
                    hFRPO_DC_SPECS_SPEC_LOWER_ACTIONS="";
                    hFRPO_DC_SPECS_CNTL_UPPER_ACTIONS="";
                    hFRPO_DC_SPECS_CNTL_LOWER_ACTIONS="";
                    hFRPO_DC_SPECS_DCITEM_TAG="";
                    hFRPO_DC_SPECS_SCRN_UPPER_REQ = 0;
                    hFRPO_DC_SPECS_SCRN_LOWER_REQ = 0;
                    hFRPO_DC_SPECS_SPEC_UPPER_REQ = 0;
                    hFRPO_DC_SPECS_SPEC_LOWER_REQ = 0;
                    hFRPO_DC_SPECS_CNTL_UPPER_REQ = 0;
                    hFRPO_DC_SPECS_CNTL_LOWER_REQ = 0;
                    hFRPO_DC_SPECS_SCRN_UPPER_LIMIT = 0.0;
                    hFRPO_DC_SPECS_SCRN_LOWER_LIMIT = 0.0;
                    hFRPO_DC_SPECS_SPEC_UPPER_LIMIT = 0.0;
                    hFRPO_DC_SPECS_SPEC_LOWER_LIMIT = 0.0;
                    hFRPO_DC_SPECS_CNTL_UPPER_LIMIT = 0.0;
                    hFRPO_DC_SPECS_CNTL_LOWER_LIMIT = 0.0;
                    hFRPO_DC_SPECS_DCITEM_TARGET    = 0.0;

                    hFRPO_DC_SPECS_DCITEM_NAME=convert(DC_2.get(j)[0]);
                    hFRPO_DC_SPECS_SCRN_UPPER_REQ=convertI(DC_2.get(j)[1]);
                    hFRPO_DC_SPECS_SCRN_UPPER_LIMIT=convertD(DC_2.get(j)[2]);
                    hFRPO_DC_SPECS_SCRN_UPPER_ACTIONS=convert(DC_2.get(j)[3]);
                    hFRPO_DC_SPECS_SCRN_LOWER_REQ=convertI(DC_2.get(j)[4]);
                    hFRPO_DC_SPECS_SCRN_LOWER_LIMIT=convertD(DC_2.get(j)[5]);
                    hFRPO_DC_SPECS_SCRN_LOWER_ACTIONS=convert(DC_2.get(j)[6]);
                    hFRPO_DC_SPECS_SPEC_UPPER_REQ=convertI(DC_2.get(j)[7]);
                    hFRPO_DC_SPECS_SPEC_UPPER_LIMIT=convertD(DC_2.get(j)[8]);
                    hFRPO_DC_SPECS_SPEC_UPPER_ACTIONS=convert(DC_2.get(j)[9]);
                    hFRPO_DC_SPECS_SPEC_LOWER_REQ=convertI(DC_2.get(j)[10]);
                    hFRPO_DC_SPECS_SPEC_LOWER_LIMIT=convertD(DC_2.get(j)[11]);
                    hFRPO_DC_SPECS_SPEC_LOWER_ACTIONS=convert(DC_2.get(j)[12]);
                    hFRPO_DC_SPECS_CNTL_UPPER_REQ=convertI(DC_2.get(j)[13]);
                    hFRPO_DC_SPECS_CNTL_UPPER_LIMIT=convertD(DC_2.get(j)[14]);
                    hFRPO_DC_SPECS_CNTL_UPPER_ACTIONS=convert(DC_2.get(j)[15]);
                    hFRPO_DC_SPECS_CNTL_LOWER_REQ=convertI(DC_2.get(j)[16]);
                    hFRPO_DC_SPECS_CNTL_LOWER_LIMIT=convertD(DC_2.get(j)[17]);
                    hFRPO_DC_SPECS_CNTL_LOWER_ACTIONS=convert(DC_2.get(j)[18]);
                    hFRPO_DC_SPECS_DCITEM_TARGET=convertD(DC_2.get(j)[19]);
                    hFRPO_DC_SPECS_DCITEM_TAG=convert(DC_2.get(j)[20]);
                    hFRPO_DC_SPECS_DC_SPEC_GROUP=convert(DC_2.get(j)[21]);

                    dcItemName .setValue(hFRPO_DC_SPECS_DCITEM_NAME);
                    screenUpperActions .setValue(hFRPO_DC_SPECS_SCRN_UPPER_ACTIONS);
                    screenLowerActions .setValue(hFRPO_DC_SPECS_SCRN_LOWER_ACTIONS);
                    specUpperActions .setValue(hFRPO_DC_SPECS_SPEC_UPPER_ACTIONS);
                    specLowerActions  .setValue(hFRPO_DC_SPECS_SPEC_LOWER_ACTIONS);
                    controlUpperActions .setValue(hFRPO_DC_SPECS_CNTL_UPPER_ACTIONS);
                    controlLowerActions .setValue(hFRPO_DC_SPECS_CNTL_LOWER_ACTIONS);
                    dcItemTag .setValue(hFRPO_DC_SPECS_DCITEM_TAG);
                    dcSpecGroup .setValue(hFRPO_DC_SPECS_DC_SPEC_GROUP);

                    if(hFRPO_DC_SPECS_SCRN_UPPER_REQ == 0)
                        screenUpperReq .setValue(0);
                    else
                        screenUpperReq .setValue(1);
                                    if(hFRPO_DC_SPECS_SCRN_LOWER_REQ == 0)
                        screenLowerReq .setValue(0);
                    else
                        screenLowerReq .setValue(1);
                                    if(hFRPO_DC_SPECS_SPEC_UPPER_REQ == 0)
                        specUpperReq .setValue(0);
                    else
                        specUpperReq .setValue(1);
                                    if(hFRPO_DC_SPECS_SPEC_LOWER_REQ == 0)
                        specLowerReq .setValue(0);
                    else
                        specLowerReq .setValue(1);
                                    if(hFRPO_DC_SPECS_CNTL_UPPER_REQ == 0)
                        controlUpperReq .setValue(0);
                    else
                        controlUpperReq .setValue(1);
                                    if(hFRPO_DC_SPECS_CNTL_LOWER_REQ == 0)
                        controlLowerReq .setValue(0);
                    else
                        controlLowerReq .setValue(1);
                    screenUpperLimit  .setValue(hFRPO_DC_SPECS_SCRN_UPPER_LIMIT);
                    screenLowerLimit  .setValue(hFRPO_DC_SPECS_SCRN_LOWER_LIMIT);
                    specUpperLimit    .setValue(hFRPO_DC_SPECS_SPEC_UPPER_LIMIT);
                    specLowerLimit    .setValue(hFRPO_DC_SPECS_SPEC_LOWER_LIMIT);
                    controlUpperLimit .setValue(hFRPO_DC_SPECS_CNTL_UPPER_LIMIT);
                    controlLowerLimit .setValue(hFRPO_DC_SPECS_CNTL_LOWER_LIMIT);
                    dcItemTarget      .setValue(hFRPO_DC_SPECS_DCITEM_TARGET);

                    // fetchFRPO_DC_SPECS
                    /*if( !isOk(iRc) ) {
                        return( iRc );
                    }

                    Object[] iRcObject=convert(iRc.getBody());

                    screenUpperReq  = convert(iRcObject[0]);
                            screenUpperLimit  = convert(iRcObject[1]);
                    screenLowerReq  = convert(iRcObject[2]);
                            screenLowerLimit  = convert(iRcObject[3]);
                    specUpperReq  = convert(iRcObject[4]);
                            specUpperLimit  = convert(iRcObject[5]);
                    specLowerReq  = convert(iRcObject[6]);
                            specLowerLimit  = convert(iRcObject[7]);
                    controlUpperReq  = convert(iRcObject[8]);
                            controlUpperLimit  = convert(iRcObject[9]);
                    controlLowerReq  = convert(iRcObject[10]);
                            controlLowerLimit  = convert(iRcObject[11]);
                    dcItemTarget  = convert(iRcObject[12]);*/


                    fhcdatahs_spec=new Infos.OhcdatahsSpec();
                    fhcdatahs_spec.setMeas_lot_id(collectedDataEventRecord.getMeasuredLotData().getLotID ());
                    fhcdatahs_spec.setMeas_mainpd_id(collectedDataEventRecord.getMeasuredLotData().getRouteID ());
                    fhcdatahs_spec.setMeas_ope_no(collectedDataEventRecord.getMeasuredLotData().getOperationNumber ());
                    fhcdatahs_spec.setMeas_pass_count   (collectedDataEventRecord.getMeasuredLotData().getOperationPassCount ());
                    fhcdatahs_spec.setClaim_time(collectedDataEventRecord.getEventCommon().getEventTimeStamp ());
                    fhcdatahs_spec.setMeas_dcdef_id(dcdefID .getValue());
                    fhcdatahs_spec.setMeas_dcdef_desc(dcdefDesc .getValue());
                    fhcdatahs_spec.setMeas_dc_type(dc_type .getValue());
                    fhcdatahs_spec.setMeas_dcspec_id(dcspecID .getValue());
                    fhcdatahs_spec.setMeas_dcspec_desc(dcspecDesc .getValue());
                    fhcdatahs_spec.setDcitem_name(dcItemName .getValue());
                    fhcdatahs_spec.setScrn_upper_req    (screenUpperReq.getValue());
                    fhcdatahs_spec.setScrn_upper_limit  (screenUpperLimit.getValue());
                    fhcdatahs_spec.setScrn_upper_actions(screenUpperActions .getValue());
                    fhcdatahs_spec.setSpec_upper_req    (specUpperReq.getValue());
                    fhcdatahs_spec.setSpec_upper_limit  (specUpperLimit.getValue());
                    fhcdatahs_spec.setSpec_upper_actions(specUpperActions .getValue());
                    fhcdatahs_spec.setCntl_upper_req    (controlUpperReq.getValue());
                    fhcdatahs_spec.setCntl_upper_limit  (controlUpperLimit.getValue());
                    fhcdatahs_spec.setCntl_upper_actions(controlUpperActions .getValue());
                    fhcdatahs_spec.setCntl_lower_req    (controlLowerReq.getValue());
                    fhcdatahs_spec.setCntl_lower_limit  (controlLowerLimit.getValue());
                    fhcdatahs_spec.setCntl_lower_actions(controlLowerActions .getValue());
                    fhcdatahs_spec.setSpec_lower_req    (specLowerReq.getValue());
                    fhcdatahs_spec.setSpec_lower_limit  (specLowerLimit.getValue());
                    fhcdatahs_spec.setSpec_lower_actions(specLowerActions .getValue());
                    fhcdatahs_spec.setScrn_lower_req    (screenLowerReq.getValue());
                    fhcdatahs_spec.setScrn_lower_limit  (screenLowerLimit.getValue());
                    fhcdatahs_spec.setScrn_lower_actions(screenLowerActions .getValue());
                    fhcdatahs_spec.setDcitem_target     (dcItemTarget.getValue());
                    fhcdatahs_spec.setDcitem_tag(dcItemTag .getValue());
                    fhcdatahs_spec.setDcspec_group(dcSpecGroup .getValue());

                    iRc = insertCollectedDataSpecHistory( fhcdatahs_spec );
                    if( !isOk(iRc) ) {
                        return( iRc );
                    }
                }

                if( count.getValue() != 0 ) {
                    if( !isOk(iRc) ) {
                        return( iRc );
                    }
                }
            }
        }

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 15:45
     */
    public Response createFHCDATAHS_LOT(Infos.CollectedDataEventRecord collectedDataEventRecord, List<Infos.UserDataSet> userDataSets ) {
        Infos.OhcdatahsLot fhcdatahs_lot=new Infos.OhcdatahsLot();
        Infos.Frlot lotData=new Infos.Frlot();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String custProdID=new Params.String();
        Infos.Frpo poData=new Infos.Frpo();
        Timestamp shopData=new Timestamp(0);
        Infos.Frpd pdData=new Infos.Frpd();
        Params.String equipmentName=new Params.String();
        Params.String areaID=new Params.String();
        int   i;
        Response             iRc = returnOK();

        List<Infos.ProcessedLotEventData>  processedLots=collectedDataEventRecord.getProcessedLots();
        for( i = 0; i < length(processedLots); i++ ) {
            Infos.ProcessedLotEventData processedLot = processedLots.get(i);
            iRc = tableMethod.getFRLOT( processedLot.getProcessLotID(), lotData );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODSPEC( lotData.getProdspec_id(), prodGrpID, prodType );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPRODGRP( lotData.getProdspec_id(), techID );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRCUSTPROD2( lotData.getCustomerID(), lotData.getProdspec_id(), custProdID );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            iRc = tableMethod.getFRPO2( processedLot.getProcessObjrefPO(),
                    poData );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRCALENDAR( poData.getActual_end_time(), shopData );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            iRc = tableMethod.getFRPD2( poData.getPd_id(), pdData );
            if( !isOk(iRc) ) {
                return( iRc );
            }


            iRc = tableMethod.getFREQP( poData.getAsgn_eqp_id(), areaID, equipmentName );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            fhcdatahs_lot=new Infos.OhcdatahsLot();
            fhcdatahs_lot.setProc_lot_id        (collectedDataEventRecord.getProcessedLots().get(i).getProcessLotID ());
            fhcdatahs_lot.setProc_lot_type      (lotData.getLotType ()) ;
            fhcdatahs_lot.setMonitor_grp_id     (collectedDataEventRecord.getMonitorGroupID ());
            fhcdatahs_lot.setProc_prodspec_id   (lotData.getProdspec_id ());
            fhcdatahs_lot.setProc_prodgrp_id    (prodGrpID.getValue() );
            fhcdatahs_lot.setProc_tech_id       (techID.getValue() );
            fhcdatahs_lot.setCustomer_id        (lotData.getCustomerID ());
            fhcdatahs_lot.setProc_custprod_id   (custProdID.getValue() );
            fhcdatahs_lot.setMeas_lot_id        (collectedDataEventRecord.getMeasuredLotData().getLotID ()) ;
            fhcdatahs_lot.setMeas_mainpd_id     (collectedDataEventRecord.getMeasuredLotData().getRouteID ()) ;
            fhcdatahs_lot.setMeas_ope_no        (collectedDataEventRecord.getMeasuredLotData().getOperationNumber ()) ;
            fhcdatahs_lot.setMeas_pass_count    (collectedDataEventRecord.getMeasuredLotData().getOperationPassCount ());
            fhcdatahs_lot.setClaim_time         (collectedDataEventRecord.getEventCommon().getEventTimeStamp ()) ;
            fhcdatahs_lot.setClaim_shop_date    (collectedDataEventRecord.getEventCommon().getEventShopDate ());
            fhcdatahs_lot.setClaim_user_id      (collectedDataEventRecord.getEventCommon().getUserID ()) ;
            fhcdatahs_lot.setProc_mainpd_id     (collectedDataEventRecord.getProcessedLots().get(i).getProcessRouteID ()) ;
            fhcdatahs_lot.setProc_ope_no        (collectedDataEventRecord.getProcessedLots().get(i).getProcessOperationNumber ()) ;
            fhcdatahs_lot.setProc_pd_id         (poData.getPd_id ());
            fhcdatahs_lot.setProc_pass_count    (collectedDataEventRecord.getProcessedLots().get(i).getProcessOperationPassCount ());
            fhcdatahs_lot.setProc_pd_name       (pdData.getOperationName ()) ;
            fhcdatahs_lot.setProc_area_id       (areaID.getValue() ) ;
            fhcdatahs_lot.setProc_eqp_id        (poData.getAsgn_eqp_id  ());
            fhcdatahs_lot.setProc_eqp_name      (equipmentName.getValue() );
            fhcdatahs_lot.setProc_lc_recipe_id  (poData.getAsgn_lcrecipe_id ());
            fhcdatahs_lot.setProc_recipe_id     (poData.getAsgn_recipe_id ());
            fhcdatahs_lot.setProc_ph_recipe_id  (poData.getAsgn_phrecipe_id ());
            fhcdatahs_lot.setCtrl_job           (poData.getCtrljob_id ());
            fhcdatahs_lot.setProc_time          (poData.getActual_end_time ());
            if( variableStrCmp( poData.getActual_end_time(), "" ) == 0
                    || poData.getActual_end_time ()== null ) {
                fhcdatahs_lot.setProc_time("1901-01-01-00.00.00.000000" );
            }
            fhcdatahs_lot.setProc_shop_date     (shopData);
            fhcdatahs_lot.setProc_wfrhs_time    ("1901-01-01-00.00.00.000000" );

            iRc = insertCollectedDataLotHistory( fhcdatahs_lot );
            if( !isOk(iRc)) {
                return( iRc );
            }
        }


        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataLotRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 10:44
     */
    public Response insertCollectedDataLotHistory(Infos.OhcdatahsLot collectedDataLotRecord ) {
        String hFHCDATAHS_LOT_PROC_LOT_ID       ;
        String hFHCDATAHS_LOT_PROC_LOT_TYPE     ;
        String hFHCDATAHS_LOT_MONITOR_GRP_ID    ;
        String hFHCDATAHS_LOT_PROC_PRODSPEC_ID  ;
        String hFHCDATAHS_LOT_PROC_PRODGRP_ID   ;
        String hFHCDATAHS_LOT_PROC_TECH_ID      ;
        String hFHCDATAHS_LOT_PROC_CUSTOMER_ID  ;
        String hFHCDATAHS_LOT_PROC_CUSTPROD_ID  ;
        String hFHCDATAHS_LOT_MEAS_LOT_ID       ;
        String hFHCDATAHS_LOT_MEAS_MAINPD_ID    ;
        String hFHCDATAHS_LOT_MEAS_OPE_NO       ;
        Integer hFHCDATAHS_LOT_MEAS_PASS_COUNT        ;
        String hFHCDATAHS_LOT_CLAIM_TIME        ;
        Double   hFHCDATAHS_LOT_CLAIM_SHOP_DATE        ;
        String hFHCDATAHS_LOT_CLAIM_USER_ID     ;
        String hFHCDATAHS_LOT_PROC_MAINPD_ID    ;
        String hFHCDATAHS_LOT_PROC_OPE_NO       ;
        String hFHCDATAHS_LOT_PROC_PD_ID        ;
        Long hFHCDATAHS_LOT_PROC_PASS_COUNT        ;
        String hFHCDATAHS_LOT_PROC_PD_NAME      ;
        String hFHCDATAHS_LOT_PROC_AREA_ID      ;
        String hFHCDATAHS_LOT_PROC_EQP_ID       ;
        String hFHCDATAHS_LOT_PROC_EQP_NAME     ;
        String hFHCDATAHS_LOT_PROC_LC_RECIPE_ID ;
        String hFHCDATAHS_LOT_PROC_RECIPE_ID    ;
        String hFHCDATAHS_LOT_PROC_PH_RECIPE_ID ;
        String hFHCDATAHS_LOT_CTRL_JOB          ;
        String hFHCDATAHS_LOT_PROC_TIME         ;
        Timestamp   hFHCDATAHS_LOT_PROC_SHOP_DATE         ;
        String hFHCDATAHS_LOT_PROC_WFRHS_TIME   ;

        hFHCDATAHS_LOT_PROC_LOT_ID      = collectedDataLotRecord.getProc_lot_id       ();
        hFHCDATAHS_LOT_PROC_LOT_TYPE    = collectedDataLotRecord.getProc_lot_type     ();
        hFHCDATAHS_LOT_MONITOR_GRP_ID   = collectedDataLotRecord.getMonitor_grp_id    ();
        hFHCDATAHS_LOT_PROC_PRODSPEC_ID = collectedDataLotRecord.getProc_prodspec_id  ();
        hFHCDATAHS_LOT_PROC_PRODGRP_ID  = collectedDataLotRecord.getProc_prodgrp_id   ();
        hFHCDATAHS_LOT_PROC_TECH_ID     = collectedDataLotRecord.getProc_tech_id      ();
        hFHCDATAHS_LOT_PROC_CUSTOMER_ID = collectedDataLotRecord.getCustomer_id       ();
        hFHCDATAHS_LOT_PROC_CUSTPROD_ID = collectedDataLotRecord.getProc_custprod_id  ();
        hFHCDATAHS_LOT_MEAS_LOT_ID      = collectedDataLotRecord.getMeas_lot_id       ();
        hFHCDATAHS_LOT_MEAS_MAINPD_ID   = collectedDataLotRecord.getMeas_mainpd_id    ();
        hFHCDATAHS_LOT_MEAS_OPE_NO      = collectedDataLotRecord.getMeas_ope_no       ();
        hFHCDATAHS_LOT_MEAS_PASS_COUNT  =  collectedDataLotRecord.getMeas_pass_count    ();
        hFHCDATAHS_LOT_CLAIM_TIME       = collectedDataLotRecord.getClaim_time        ();
        hFHCDATAHS_LOT_CLAIM_SHOP_DATE  =  collectedDataLotRecord.getClaim_shop_date    ();
        hFHCDATAHS_LOT_CLAIM_USER_ID    = collectedDataLotRecord.getClaim_user_id     ();
        hFHCDATAHS_LOT_PROC_MAINPD_ID   = collectedDataLotRecord.getProc_mainpd_id    ();
        hFHCDATAHS_LOT_PROC_OPE_NO      = collectedDataLotRecord.getProc_ope_no       ();
        hFHCDATAHS_LOT_PROC_PD_ID       = collectedDataLotRecord.getProc_pd_id        ();
        hFHCDATAHS_LOT_PROC_PASS_COUNT  =  collectedDataLotRecord.getProc_pass_count    ();
        hFHCDATAHS_LOT_PROC_PD_NAME     = collectedDataLotRecord.getProc_pd_name      ();
        hFHCDATAHS_LOT_PROC_AREA_ID     = collectedDataLotRecord.getProc_area_id      ();
        hFHCDATAHS_LOT_PROC_EQP_ID      = collectedDataLotRecord.getProc_eqp_id       ();
        hFHCDATAHS_LOT_PROC_EQP_NAME    = collectedDataLotRecord.getProc_eqp_name     ();
        hFHCDATAHS_LOT_PROC_LC_RECIPE_ID= collectedDataLotRecord.getProc_lc_recipe_id ();
        hFHCDATAHS_LOT_PROC_RECIPE_ID   = collectedDataLotRecord.getProc_recipe_id    ();
        hFHCDATAHS_LOT_PROC_PH_RECIPE_ID= collectedDataLotRecord.getProc_ph_recipe_id ();
        hFHCDATAHS_LOT_CTRL_JOB         = collectedDataLotRecord.getCtrl_job          ();
        hFHCDATAHS_LOT_PROC_TIME        = collectedDataLotRecord.getProc_time         ();
        hFHCDATAHS_LOT_PROC_SHOP_DATE   =  collectedDataLotRecord.getProc_shop_date     ();
        hFHCDATAHS_LOT_PROC_WFRHS_TIME  = collectedDataLotRecord.getProc_wfrhs_time   ();

        baseCore.insert("INSERT INTO OHEDC_LOT\n" +
                "                (  ID, PROC_LOT_ID      ,\n" +
                "                        PROC_LOT_TYPE    ,\n" +
                "                        MON_GRP_ID   ,\n" +
                "                        PROC_PRODSPEC_ID ,\n" +
                "                        PROC_PRODGRP_ID  ,\n" +
                "                        PROC_TECH_ID     ,\n" +
                "                        PROC_CUSTOMER_ID ,\n" +
                "                        PROC_CUSTPROD_ID ,\n" +
                "                        MEAS_LOT_ID      ,\n" +
                "                        MEAS_PROCESS_ID   ,\n" +
                "                        MEAS_OPE_NO      ,\n" +
                "                        MEAS_PASS_COUNT  ,\n" +
                "                        TRX_TIME       ,\n" +
                "                        TRX_WORK_DATE  ,\n" +
                "                        TRX_USER_ID    ,\n" +
                "                        PROC_PROCESS_ID   ,\n" +
                "                        PROC_OPE_NO      ,\n" +
                "                        PROC_STEP_ID       ,\n" +
                "                        PROC_PASS_COUNT  ,\n" +
                "                        PROC_PD_NAME     ,\n" +
                "                        PROC_AREA_ID     ,\n" +
                "                        PROC_EQP_ID      ,\n" +
                "                        PROC_EQP_NAME    ,\n" +
                "                        PROC_LRCP_ID,\n" +
                "                        PROC_MRCP_ID   ,\n" +
                "                        PROC_PRCP_ID,\n" +
                "                        CJ_ID         ,\n" +
                "                        PROC_TIME        ,\n" +
                "                        PROC_WAFER_HIS_TIME  ,\n" +
                "                        STORE_TIME )\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                CURRENT_TIMESTAMP                )",generateID(Infos.OhcdatahsLot.class)
                ,hFHCDATAHS_LOT_PROC_LOT_ID
                , hFHCDATAHS_LOT_PROC_LOT_TYPE
                , hFHCDATAHS_LOT_MONITOR_GRP_ID
                , hFHCDATAHS_LOT_PROC_PRODSPEC_ID
                , hFHCDATAHS_LOT_PROC_PRODGRP_ID
                , hFHCDATAHS_LOT_PROC_TECH_ID
                , hFHCDATAHS_LOT_PROC_CUSTOMER_ID
                , hFHCDATAHS_LOT_PROC_CUSTPROD_ID
                , hFHCDATAHS_LOT_MEAS_LOT_ID
                , hFHCDATAHS_LOT_MEAS_MAINPD_ID
                , hFHCDATAHS_LOT_MEAS_OPE_NO
                , hFHCDATAHS_LOT_MEAS_PASS_COUNT
                , convert(hFHCDATAHS_LOT_CLAIM_TIME)
                , hFHCDATAHS_LOT_CLAIM_SHOP_DATE
                , hFHCDATAHS_LOT_CLAIM_USER_ID
                , hFHCDATAHS_LOT_PROC_MAINPD_ID
                , hFHCDATAHS_LOT_PROC_OPE_NO
                , hFHCDATAHS_LOT_PROC_PD_ID
                , hFHCDATAHS_LOT_PROC_PASS_COUNT
                , hFHCDATAHS_LOT_PROC_PD_NAME
                , hFHCDATAHS_LOT_PROC_AREA_ID
                , hFHCDATAHS_LOT_PROC_EQP_ID
                , hFHCDATAHS_LOT_PROC_EQP_NAME
                , hFHCDATAHS_LOT_PROC_LC_RECIPE_ID
                , hFHCDATAHS_LOT_PROC_RECIPE_ID
                , hFHCDATAHS_LOT_PROC_PH_RECIPE_ID
                , hFHCDATAHS_LOT_CTRL_JOB
                , convert(hFHCDATAHS_LOT_PROC_TIME)
                , convert(hFHCDATAHS_LOT_PROC_WFRHS_TIME));


        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param CollectedDataEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 15:32
     */
    public Response createFHCDATAHS(Infos.CollectedDataEventRecord CollectedDataEventRecord,
                     List<Infos.UserDataSet> userDataSets) {
        Infos.Ohcdatahs fhcdatahs=new Infos.Ohcdatahs();
        Infos.Frpd pdData=new Infos.Frpd();
        Params.String prodGrpID=new Params.String();
        Params.String prodType=new Params.String();
        Params.String techID=new Params.String();
        Params.String equipmentName=new Params.String();
        Params.String areaID=new Params.String();
        Params.String dcdefID=new Params.String();
        Params.String dcdefDesc=new Params.String();
        Params.String dc_type=new Params.String();
        Params.String dcspecID=new Params.String();
        Params.String dcspecDesc=new Params.String();
        Params.Param<Integer> d_SeqNO=new Params.Param<>();
        Response iRc = returnOK();

        d_SeqNO.setValue(0);

        iRc = tableMethod.getFRPRODSPEC( CollectedDataEventRecord.getMeasuredLotData().getProductID(), prodGrpID, prodType );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPRODGRP( CollectedDataEventRecord.getMeasuredLotData().getProductID(), techID );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFRPD2( CollectedDataEventRecord.getMeasuredLotData().getOperationID(), pdData );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = tableMethod.getFREQP( CollectedDataEventRecord.getEquipmentID(), areaID, equipmentName );
        if( !isOk(iRc) ) {
            return( iRc );
        }
        iRc = getFRPO_DC( CollectedDataEventRecord.getMeasuredLotData().getObjrefPO(),
                dcdefID,
                dcdefDesc,
                dc_type,
                dcspecID,
                dcspecDesc,
                d_SeqNO );

        if( !isOk(iRc) ) {
            return( iRc );
        }

        fhcdatahs=new Infos.Ohcdatahs();
        fhcdatahs.setMeas_lot_id(CollectedDataEventRecord.getMeasuredLotData().getLotID ());
        fhcdatahs.setMeas_lot_type(CollectedDataEventRecord.getMeasuredLotData().getLotType ());
        fhcdatahs.setMeas_prodspec_id(CollectedDataEventRecord.getMeasuredLotData().getProductID ());
        fhcdatahs.setMeas_prodgrp_id(prodGrpID.getValue() );
        fhcdatahs.setMeas_tech_id(techID.getValue() );
        fhcdatahs.setMonitor_grp_id(CollectedDataEventRecord.getMonitorGroupID ());
        fhcdatahs.setMeas_mainpd_id(CollectedDataEventRecord.getMeasuredLotData().getRouteID ());
        fhcdatahs.setMeas_ope_no(CollectedDataEventRecord.getMeasuredLotData().getOperationNumber ());
        fhcdatahs.setMeas_pd_id(CollectedDataEventRecord.getMeasuredLotData().getOperationID ());
        fhcdatahs.setMeas_pd_type(pdData.getPd_type());

        fhcdatahs.setMeas_pass_count  (CollectedDataEventRecord.getMeasuredLotData().getOperationPassCount ());
        fhcdatahs.setMeas_pd_name(pdData.getOperationName ());
        fhcdatahs.setMeas_area_id(areaID.getValue() );
        fhcdatahs.setMeas_eqp_id(CollectedDataEventRecord.getEquipmentID ());
        fhcdatahs.setMeas_eqp_name(equipmentName.getValue() );
        fhcdatahs.setMeas_lc_recipe_id(CollectedDataEventRecord.getLogicalRecipeID ());
        fhcdatahs.setMeas_recipe_id(CollectedDataEventRecord.getMachineRecipeID ());
        fhcdatahs.setMeas_ph_recipe_id(CollectedDataEventRecord.getPhysicalRecipeID ());
        fhcdatahs.setClaim_time(CollectedDataEventRecord.getEventCommon().getEventTimeStamp ());
        fhcdatahs.setClaim_shop_date  (CollectedDataEventRecord.getEventCommon().getEventShopDate ());
        fhcdatahs.setClaim_user_id(CollectedDataEventRecord.getEventCommon().getUserID ());
        fhcdatahs.setMeas_dcdef_id(dcdefID.getValue() );
        fhcdatahs.setMeas_dc_type(dc_type.getValue() );
        fhcdatahs.setMeas_dcspec_id(dcspecID.getValue() );
        fhcdatahs.setMeas_wfrhs_time(CollectedDataEventRecord.getMeasuredLotData().getWaferHistoryTimeStamp ());
        fhcdatahs.setEvent_create_time(CollectedDataEventRecord.getEventCommon().getEventCreationTimeStamp ());

        iRc = insertCollectedDataHistory( fhcdatahs );
        if( !isOk(iRc)) {
            return( iRc );
        }

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 11:40
     */
    public Response insertCollectedDataHistory(Infos.Ohcdatahs collectedDataRecord ) {
        String  hFHCDATAHS_MEAS_LOT_ID       ;
        String  hFHCDATAHS_MEAS_LOT_TYPE     ;
        String  hFHCDATAHS_MEAS_PRODSPEC_ID  ;
        String  hFHCDATAHS_MEAS_PRODGRP_ID   ;
        String  hFHCDATAHS_MEAS_TECH_ID      ;
        String  hFHCDATAHS_MONITOR_GRP_ID    ;
        String  hFHCDATAHS_MEAS_MAINPD_ID    ;
        String  hFHCDATAHS_MESA_OPE_NO       ;
        String  hFHCDATAHS_MEAS_PD_ID        ;
        String  hFHCDATAHS_MEAS_PD_TYPE      ;
        Integer hFHCDATAHS_MEAS_PASS_COUNT       ;
        String  hFHCDATAHS_MEAS_PD_NAME      ;
        String  hFHCDATAHS_MEAS_AREA_ID      ;
        String  hFHCDATAHS_MEAS_EQP_ID       ;
        String  hFHCDATAHS_MEAS_EQP_NAME     ;
        String  hFHCDATAHS_MEAS_LC_RECIPE_ID ;
        String  hFHCDATAHS_MEAS_RECIPE_ID    ;
        String  hFHCDATAHS_MEAS_PH_RECIPE_ID ;
        String  hFHCDATAHS_CLAIM_TIME        ;
        Double  hFHCDATAHS_CLAIM_SHOP_DATE       ;
        String  hFHCDATAHS_CLAIM_USER_ID     ;
        String  hFHCDATAHS_MEAS_DCDEF_ID     ;
        String  hFHCDATAHS_MEAS_DC_TYPE      ;
        String  hFHCDATAHS_MEAS_DCSPEC_ID    ;
        String  hFHCDATAHS_MEAS_WFRHS_TIME   ;
        String  hFHCDATAHS_EVENT_CREATE_TIME ;

        hFHCDATAHS_MEAS_LOT_ID = collectedDataRecord.getMeas_lot_id      ();
        hFHCDATAHS_MEAS_LOT_TYPE = collectedDataRecord.getMeas_lot_type    ();
        hFHCDATAHS_MEAS_PRODSPEC_ID = collectedDataRecord.getMeas_prodspec_id ();
        hFHCDATAHS_MEAS_PRODGRP_ID = collectedDataRecord.getMeas_prodgrp_id  ();
        hFHCDATAHS_MEAS_TECH_ID = collectedDataRecord.getMeas_tech_id     ();
        hFHCDATAHS_MONITOR_GRP_ID = collectedDataRecord.getMonitor_grp_id   ();
        hFHCDATAHS_MEAS_MAINPD_ID = collectedDataRecord.getMeas_mainpd_id   ();
        hFHCDATAHS_MESA_OPE_NO = collectedDataRecord.getMeas_ope_no      ();
        hFHCDATAHS_MEAS_PD_ID = collectedDataRecord.getMeas_pd_id       ();
        hFHCDATAHS_MEAS_PD_TYPE = collectedDataRecord.getMeas_pd_type     ();
        hFHCDATAHS_MEAS_PASS_COUNT  = collectedDataRecord.getMeas_pass_count   ();
        hFHCDATAHS_MEAS_PD_NAME = collectedDataRecord.getMeas_pd_name     ();
        hFHCDATAHS_MEAS_AREA_ID = collectedDataRecord.getMeas_area_id     ();
        hFHCDATAHS_MEAS_EQP_ID = collectedDataRecord.getMeas_eqp_id      ();
        hFHCDATAHS_MEAS_EQP_NAME = collectedDataRecord.getMeas_eqp_name    ();
        hFHCDATAHS_MEAS_LC_RECIPE_ID = collectedDataRecord.getMeas_lc_recipe_id();
        hFHCDATAHS_MEAS_RECIPE_ID = collectedDataRecord.getMeas_recipe_id   ();
        hFHCDATAHS_MEAS_PH_RECIPE_ID = collectedDataRecord.getMeas_ph_recipe_id();
        hFHCDATAHS_CLAIM_TIME = collectedDataRecord.getClaim_time       ();
        hFHCDATAHS_CLAIM_SHOP_DATE  = collectedDataRecord.getClaim_shop_date   ();
        hFHCDATAHS_CLAIM_USER_ID = collectedDataRecord.getClaim_user_id    ();
        hFHCDATAHS_MEAS_DCDEF_ID = collectedDataRecord.getMeas_dcdef_id    ();
        hFHCDATAHS_MEAS_DC_TYPE = collectedDataRecord.getMeas_dc_type     ();
        hFHCDATAHS_MEAS_DCSPEC_ID = collectedDataRecord.getMeas_dcspec_id   ();
        hFHCDATAHS_MEAS_WFRHS_TIME = collectedDataRecord.getMeas_wfrhs_time  ();
        hFHCDATAHS_EVENT_CREATE_TIME = collectedDataRecord.getEvent_create_time ();

        baseCore.insert("INSERT INTO OHEDC\n" +
                "                (  ID,MEAS_LOT_ID,\n" +
                "                        MEAS_LOT_TYPE,\n" +
                "                        MEAS_PROD_ID,\n" +
                "                        MEAS_PRODFMLY_ID,\n" +
                "                        MEAS_TECH_ID,\n" +
                "                        MON_GRP_ID,\n" +
                "                        MEAS_PROCESS_ID,\n" +
                "                        MEAS_OPE_NO,\n" +
                "                        MEAS_STEP_ID,\n" +
                "                        MEAS_STEP_TYPE,\n" +
                "                        MEAS_PASS_COUNT,\n" +
                "                        MEAS_STEP_NAME,\n" +
                "                        MEAS_BAY_ID,\n" +
                "                        MEAS_EQP_ID,\n" +
                "                        MEAS_EQP_NAME,\n" +
                "                        MEAS_LRCP_ID,\n" +
                "                        MEAS_MRCP_ID,\n" +
                "                        MEAS_PRCP_ID,\n" +
                "                        TRX_TIME,\n" +
                "                        TRX_WORK_DATE,\n" +
                "                        TRX_USER_ID,\n" +
                "                        MEAS_EDC_PLAN_ID,\n" +
                "                        MEAS_EDC_TYPE,\n" +
                "                        MEAS_EDC_SPEC_ID,\n" +
                "                        MEAS_WAFER_HIS_TIME,\n" +
                "                        EVENT_CREATE_TIME,\n" +
                "                        STORE_TIME )\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                CURRENT_TIMESTAMP            )",generateID(Infos.Ohcdatahs.class)
                ,hFHCDATAHS_MEAS_LOT_ID
                , hFHCDATAHS_MEAS_LOT_TYPE
                , hFHCDATAHS_MEAS_PRODSPEC_ID
                , hFHCDATAHS_MEAS_PRODGRP_ID
                , hFHCDATAHS_MEAS_TECH_ID
                , hFHCDATAHS_MONITOR_GRP_ID
                , hFHCDATAHS_MEAS_MAINPD_ID
                , hFHCDATAHS_MESA_OPE_NO
                , hFHCDATAHS_MEAS_PD_ID
                , hFHCDATAHS_MEAS_PD_TYPE
                , hFHCDATAHS_MEAS_PASS_COUNT
                , hFHCDATAHS_MEAS_PD_NAME
                , hFHCDATAHS_MEAS_AREA_ID
                , hFHCDATAHS_MEAS_EQP_ID
                , hFHCDATAHS_MEAS_EQP_NAME
                , hFHCDATAHS_MEAS_LC_RECIPE_ID
                , hFHCDATAHS_MEAS_RECIPE_ID
                , hFHCDATAHS_MEAS_PH_RECIPE_ID
                , convert(hFHCDATAHS_CLAIM_TIME)
                , hFHCDATAHS_CLAIM_SHOP_DATE
                , hFHCDATAHS_CLAIM_USER_ID
                , hFHCDATAHS_MEAS_DCDEF_ID
                , hFHCDATAHS_MEAS_DC_TYPE
                , hFHCDATAHS_MEAS_DCSPEC_ID
                , convert(hFHCDATAHS_MEAS_WFRHS_TIME)
                , convert(hFHCDATAHS_EVENT_CREATE_TIME));


        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataEventRecord
     * @param userDataSets
     * @param setFlag
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 15:24
     */
    public Response createFHCDATAHS_DATA(Infos.CollectedDataEventRecord collectedDataEventRecord,
                                         List<Infos.UserDataSet> userDataSets, Params.Param<Boolean> setFlag) {
        Infos.OhcdatahsData fhcdatahs_data;
        Params.String dcdefID=new Params.String();
        Params.String dcdefDesc=new Params.String();
        Params.String dc_type=new Params.String();
        Params.String dcspecID=new Params.String();
        Params.String dcspecDesc=new Params.String();
        Params.String measType=new Params.String();
        Params.String dcitemName=new Params.String();
        Params.String waferID=new Params.String();
        Params.String prcsJob=new Params.String();
        Params.String waferPosition=new Params.String();
        Params.String sitePosition=new Params.String();
        Params.String dcitemValue=new Params.String();
        Params.String dcmode=new Params.String();
        Params.String dcunit=new Params.String();
        Params.String data_type=new Params.String();
        Params.String item_type=new Params.String();
        Params.String specchk_result=new Params.String();
        Params.String act_code=new Params.String();
        Params.Param<Integer> d_SeqNO=new Params.Param<>();
        Params.Param<Integer>           count=new Params.Param<>();
        int           i,j;
        int           storeFlag=0;
        Params.Param<Integer>           rec_count=new Params.Param<>();
        Params.Param<Double>                  scrn_upper_limit=new Params.Param<>();
        Params.Param<Double>                  spec_upper_limit=new Params.Param<>();
        Params.Param<Double>                  ctrl_upper_limit=new Params.Param<>();
        Params.Param<Double>                  dcitem_target=new Params.Param<>();
        Params.Param<Double>                  scrn_lower_limit=new Params.Param<>();
        Params.Param<Double>                  spec_lower_limit=new Params.Param<>();
        Params.Param<Double>                  ctrl_lower_limit=new Params.Param<>();
        Response                     iRc = returnOK();

        setFlag.setValue(false);

        Map<String,String> aProcessWaferIDList=new HashMap<>();
        Map<String,String> aProcessPositionList=new HashMap<>();
        {
            iRc = selectFRPO_SMPL( collectedDataEventRecord.getMeasuredLotData().getObjrefPO() );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            Infos.FrpoSmpl poSmpl;
            List<Object[]> FRPO_SMPLs=convert(iRc.getBody());
            for( Object[]  FRPO_SMPL:  FRPO_SMPLs) {
                poSmpl=new Infos.FrpoSmpl();

                String hFRPO_SMPL_SMPL_WAFER_ID;
                Boolean       hFRPO_SMPL_SMPL_FLAG;
                String hFRPO_SMPL_PRCSJOB_ID;
                String hFRPO_SMPL_PRCSJOB_POS;

                hFRPO_SMPL_SMPL_WAFER_ID=convert(FRPO_SMPL[0]);
                hFRPO_SMPL_SMPL_FLAG=convertB(FRPO_SMPL[1]);
                hFRPO_SMPL_PRCSJOB_ID=convert(FRPO_SMPL[2]);
                hFRPO_SMPL_PRCSJOB_POS=convert(FRPO_SMPL[3]);


                poSmpl.setWafer_id(hFRPO_SMPL_SMPL_WAFER_ID);
                poSmpl.setSmpl_flg (hFRPO_SMPL_SMPL_FLAG);
                poSmpl.setPrcs_job(hFRPO_SMPL_PRCSJOB_ID);
                poSmpl.setPrcs_position(hFRPO_SMPL_PRCSJOB_POS);

                if( !aProcessWaferIDList.containsKey( poSmpl.getWafer_id() ) ) {
                    aProcessWaferIDList.put(poSmpl.getWafer_id(), poSmpl.getPrcs_job());
                }

                if( !aProcessPositionList.containsKey( poSmpl.getPrcs_position() ) ) {
                    aProcessPositionList.put( poSmpl.getPrcs_position(), (poSmpl.getPrcs_job()));
                }
            }

        }
        iRc = countFRPO_DC( collectedDataEventRecord.getMeasuredLotData().getObjrefPO(),
                rec_count );

        if( !isOk(iRc) ) {
            return( iRc );
        }

        for( j = 0; j < rec_count.getT() ; j++ ) {
            d_SeqNO .setT( j);

            iRc = getFRPO_DC( collectedDataEventRecord.getMeasuredLotData().getObjrefPO(),
                    dcdefID,
                    dcdefDesc,
                    dc_type,
                    dcspecID,
                    dcspecDesc,
                    d_SeqNO );
            if( !isOk(iRc) ) {
                return( iRc );
            }
            count .setT(0);


            iRc = selectFRPO_DC_ITEMS( collectedDataEventRecord.getMeasuredLotData().getObjrefPO(),
                    d_SeqNO,
                    count );
            if( !isOk(iRc) ) {
                return( iRc );
            }

            List<Object[]> DC_1=convert(iRc.getBody());

            for( i = 0; i < count.getValue(); i++ ) {
                String hFRPO_DC_ITEMS_DCDATA_ITEM    ;
                String hFRPO_DC_ITEMS_WAFER_ID       ;
                String hFRPO_DC_ITEMS_WAFER_POSITION ;
                String hFRPO_DC_ITEMS_SITE_POSITION  ;
                String hFRPO_DC_ITEMS_DCDATA_VALUE   ;
                String hFRPO_DC_ITEMS_DCMODE         ;
                String hFRPO_DC_ITEMS_DCUNIT         ;
                String hFRPO_DC_ITEMS_DATA_TYPE      ;
                String hFRPO_DC_ITEMS_ITEM_TYPE      ;
                String hFRPO_DC_ITEMS_SPECCHK_RESULT ;
                String hFRPO_DC_ITEMS_ACT_CODE      ;
                String hFRPO_DC_ITEMS_MEAS_TYPE      ;
                Boolean hFRPO_DC_ITEMS_HISTORY_FLG;

                hFRPO_DC_ITEMS_DCDATA_ITEM="";
                hFRPO_DC_ITEMS_WAFER_ID="";
                hFRPO_DC_ITEMS_WAFER_POSITION="";
                hFRPO_DC_ITEMS_SITE_POSITION="";
                hFRPO_DC_ITEMS_DCDATA_VALUE="";
                hFRPO_DC_ITEMS_DCMODE="";
                hFRPO_DC_ITEMS_DCUNIT="";
                hFRPO_DC_ITEMS_DATA_TYPE="";
                hFRPO_DC_ITEMS_ITEM_TYPE="";
                hFRPO_DC_ITEMS_SPECCHK_RESULT="";
                hFRPO_DC_ITEMS_ACT_CODE="";
                hFRPO_DC_ITEMS_MEAS_TYPE="";
                hFRPO_DC_ITEMS_HISTORY_FLG = false;

                hFRPO_DC_ITEMS_DCDATA_ITEM=convert(DC_1.get(i)[0]);
                hFRPO_DC_ITEMS_WAFER_ID=convert(DC_1.get(i)[1]);
                hFRPO_DC_ITEMS_WAFER_POSITION=convert(DC_1.get(i)[2]);
                hFRPO_DC_ITEMS_SITE_POSITION=convert(DC_1.get(i)[3]);
                hFRPO_DC_ITEMS_DCDATA_VALUE=convert(DC_1.get(i)[4]);
                hFRPO_DC_ITEMS_DCMODE=convert(DC_1.get(i)[5]);
                hFRPO_DC_ITEMS_DCUNIT=convert(DC_1.get(i)[6]);
                hFRPO_DC_ITEMS_DATA_TYPE=convert(DC_1.get(i)[7]);
                hFRPO_DC_ITEMS_ITEM_TYPE=convert(DC_1.get(i)[8]);
                hFRPO_DC_ITEMS_SPECCHK_RESULT=convert(DC_1.get(i)[9]);
                hFRPO_DC_ITEMS_ACT_CODE=convert(DC_1.get(i)[10]);
                hFRPO_DC_ITEMS_MEAS_TYPE=convert(DC_1.get(i)[11]);
                hFRPO_DC_ITEMS_HISTORY_FLG=convertB(DC_1.get(i)[12]);

                dcitemName.setValue(hFRPO_DC_ITEMS_DCDATA_ITEM);
                waferID.setValue(hFRPO_DC_ITEMS_WAFER_ID);
                waferPosition.setValue(hFRPO_DC_ITEMS_WAFER_POSITION);
                sitePosition.setValue(hFRPO_DC_ITEMS_SITE_POSITION);
                dcitemValue .setValue(hFRPO_DC_ITEMS_DCDATA_VALUE );
                dcmode.setValue(hFRPO_DC_ITEMS_DCMODE);
                dcunit.setValue(hFRPO_DC_ITEMS_DCUNIT);
                data_type.setValue(hFRPO_DC_ITEMS_DATA_TYPE);
                item_type.setValue(hFRPO_DC_ITEMS_ITEM_TYPE);
                specchk_result.setValue(hFRPO_DC_ITEMS_SPECCHK_RESULT);
                act_code.setValue(hFRPO_DC_ITEMS_ACT_CODE);
                measType.setValue(hFRPO_DC_ITEMS_MEAS_TYPE);
                if( !isTrue(hFRPO_DC_ITEMS_HISTORY_FLG )){
                    storeFlag = 0;
                } else {
                    storeFlag = 1;
                }

                iRc = getFRDCSPEC_ITEM( dcspecID, dcitemName,
                        scrn_upper_limit, spec_upper_limit, ctrl_upper_limit,
                                    dcitem_target,
                                    scrn_lower_limit, spec_lower_limit, ctrl_lower_limit );
                if( !isOk(iRc) ) {
                    return( iRc );
                }


                if( storeFlag==1 ) {
                    String pjID = null;
                    if ( 0 == variableStrCmp(measType.getValue(), SPConstant.SP_DCDEF_MEAS_PJWAFER )
                            || 0 == variableStrCmp(measType.getValue(), SPConstant.SP_DCDEF_MEAS_PJWAFERSITE ) ) {
                        pjID=aProcessWaferIDList.get(waferID.getValue());
                    } else if ( 0 == variableStrCmp( measType.getValue(), SPConstant.SP_DCDEF_MEAS_PJ ) ) {
                        pjID=aProcessPositionList.get(waferPosition.getValue());
                    }

                    fhcdatahs_data=new Infos.OhcdatahsData();
                    fhcdatahs_data.setMeas_lot_id(collectedDataEventRecord.getMeasuredLotData().getLotID ());
                    fhcdatahs_data.setMeas_mainpd_id(collectedDataEventRecord.getMeasuredLotData().getRouteID ());
                    fhcdatahs_data.setMeas_ope_no(collectedDataEventRecord.getMeasuredLotData().getOperationNumber ());
                    fhcdatahs_data.setMeas_pass_count (collectedDataEventRecord.getMeasuredLotData().getOperationPassCount ());
                    fhcdatahs_data.setClaim_time(collectedDataEventRecord.getEventCommon().getEventTimeStamp ());
                    fhcdatahs_data.setMeas_dcdef_id(dcdefID.getValue() );
                    fhcdatahs_data.setDcitem_name(dcitemName.getValue() );
                    fhcdatahs_data.setDcmode(dcmode.getValue() );
                    fhcdatahs_data.setDcunit(dcunit.getValue() );
                    fhcdatahs_data.setData_type(data_type.getValue() );
                    fhcdatahs_data.setItem_type(item_type.getValue() );
                    fhcdatahs_data.setMeas_type(measType.getValue() );
                    fhcdatahs_data.setWafer_id(waferID.getValue() );
                    fhcdatahs_data.setPrcs_job(pjID );
                    fhcdatahs_data.setWafer_position(waferPosition.getValue() );
                    fhcdatahs_data.setSite_position(sitePosition.getValue() );
                    fhcdatahs_data.setDcitem_value(dcitemValue.getValue() );
                    fhcdatahs_data.setSpecchk_result(specchk_result.getValue() );
                    fhcdatahs_data.setAct_code(act_code.getValue() );
                    fhcdatahs_data.setScrn_upper_limit (scrn_upper_limit.getValue() );
                    fhcdatahs_data.setSpec_upper_limit (spec_upper_limit.getValue() );
                    fhcdatahs_data.setCtrl_upper_limit (ctrl_upper_limit.getValue() );
                    fhcdatahs_data.setDcitem_target    (dcitem_target   .getValue() );
                    fhcdatahs_data.setCtrl_lower_limit (ctrl_lower_limit.getValue() );
                    fhcdatahs_data.setSpec_lower_limit (spec_lower_limit.getValue() );
                    fhcdatahs_data.setScrn_lower_limit (scrn_lower_limit.getValue() );

                    iRc = insertCollectedDataDataHistory( fhcdatahs_data );
                    if( !isOk(iRc) ) {
                        return( iRc );
                    }

                    setFlag.setValue(true);

                }

            }

            if(count.getValue()!=0) {
                if( !isOk(iRc) )  {
                    return( iRc );
                }
            }
        }

        iRc=returnOK();
        return(iRc);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataDataRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 14:52
     */
    public Response insertCollectedDataDataHistory(Infos.OhcdatahsData collectedDataDataRecord) {
        String hFHCDATAHS_DATA_MEAS_LOT_ID    =null;
        String hFHCDATAHS_DATA_MEAS_MAINPD_ID =null;
        String hFHCDATAHS_DATA_MESA_OPE_NO    =null;
        Integer hFHCDATAHS_DATA_MEAS_PASS_COUNT =null;
        String hFHCDATAHS_DATA_CLAIM_TIME     =null;
        String hFHCDATAHS_DATA_MEAS_DCDEF_ID  =null;
        String hFHCDATAHS_DATA_DCITEM_NAME    =null;
        String hFHCDATAHS_DATA_DCMODE         =null;
        String hFHCDATAHS_DATA_DCUNIT         =null;
        String hFHCDATAHS_DATA_DATA_TYPE      =null;
        String hFHCDATAHS_DATA_ITEM_TYPE      =null;
        String hFHCDATAHS_DATA_MEAS_TYPE      =null;
        String hFHCDATAHS_DATA_WAFER_ID       =null;
        String hFHCDATAHS_DATA_WAFER_POSITION =null;
        String hFHCDATAHS_DATA_SITE_POSITION  =null;
        String hFHCDATAHS_DATA_DCITEM_VALUE   =null;
        String hFHCDATAHS_DATA_SPECCHK_RESULT =null;
        String hFHCDATAHS_DATA_ACT_CODE     =null;
        Double  hFHCDATAHS_DATA_SCRN_UPPER_LIMIT   =null;
        Double  hFHCDATAHS_DATA_SPEC_UPPER_LIMIT   =null;
        Double  hFHCDATAHS_DATA_CNTL_UPPER_LIMIT   =null;
        Double  hFHCDATAHS_DATA_DCITEM_TARGET      =null;
        Double  hFHCDATAHS_DATA_SCRN_LOWER_LIMIT   =null;
        Double  hFHCDATAHS_DATA_SPEC_LOWER_LIMIT   =null;
        Double  hFHCDATAHS_DATA_CNTL_LOWER_LIMIT   =null;
        String hFHCDATAHS_DATA_PRCSJOB_ID     =null;

        hFHCDATAHS_DATA_MEAS_LOT_ID = collectedDataDataRecord.getMeas_lot_id     ();
        hFHCDATAHS_DATA_MEAS_MAINPD_ID = collectedDataDataRecord.getMeas_mainpd_id  ();
        hFHCDATAHS_DATA_MESA_OPE_NO = collectedDataDataRecord.getMeas_ope_no     ();
        hFHCDATAHS_DATA_MEAS_PASS_COUNT = collectedDataDataRecord.getMeas_pass_count();
        hFHCDATAHS_DATA_CLAIM_TIME = collectedDataDataRecord.getClaim_time      ();
        hFHCDATAHS_DATA_MEAS_DCDEF_ID = collectedDataDataRecord.getMeas_dcdef_id   ();
        hFHCDATAHS_DATA_DCITEM_NAME = collectedDataDataRecord.getDcitem_name     ();
        hFHCDATAHS_DATA_DCMODE = collectedDataDataRecord.getDcmode          ();
        hFHCDATAHS_DATA_DCUNIT = collectedDataDataRecord.getDcunit          ();
        hFHCDATAHS_DATA_DATA_TYPE = collectedDataDataRecord.getData_type       ();
        hFHCDATAHS_DATA_ITEM_TYPE = collectedDataDataRecord.getItem_type       ();
        hFHCDATAHS_DATA_MEAS_TYPE = collectedDataDataRecord.getMeas_type       ();
        hFHCDATAHS_DATA_WAFER_ID = collectedDataDataRecord.getWafer_id        ();
        hFHCDATAHS_DATA_WAFER_POSITION = collectedDataDataRecord.getWafer_position  ();
        hFHCDATAHS_DATA_SITE_POSITION = collectedDataDataRecord.getSite_position   ();
        hFHCDATAHS_DATA_DCITEM_VALUE = collectedDataDataRecord.getDcitem_value    ();
        hFHCDATAHS_DATA_SPECCHK_RESULT = collectedDataDataRecord.getSpecchk_result  ();
        hFHCDATAHS_DATA_ACT_CODE = collectedDataDataRecord.getAct_code        ();
        hFHCDATAHS_DATA_SCRN_UPPER_LIMIT                  = collectedDataDataRecord.getScrn_upper_limit ();
        hFHCDATAHS_DATA_SPEC_UPPER_LIMIT                  = collectedDataDataRecord.getSpec_upper_limit ();
        hFHCDATAHS_DATA_CNTL_UPPER_LIMIT                  = collectedDataDataRecord.getCtrl_upper_limit ();
        hFHCDATAHS_DATA_DCITEM_TARGET                     = collectedDataDataRecord.getDcitem_target    ();
        hFHCDATAHS_DATA_SCRN_LOWER_LIMIT                  = collectedDataDataRecord.getScrn_lower_limit();
        hFHCDATAHS_DATA_SPEC_LOWER_LIMIT                  = collectedDataDataRecord.getSpec_lower_limit();
        hFHCDATAHS_DATA_CNTL_LOWER_LIMIT                  = collectedDataDataRecord.getCtrl_lower_limit();
        hFHCDATAHS_DATA_PRCSJOB_ID = collectedDataDataRecord.getPrcs_job        ();

        baseCore.insert("INSERT INTO OHEDC_DATA\n" +
                "                (  ID,MEAS_LOT_ID,\n" +
                "                        MEAS_PROCESS_ID,\n" +
                "                        MEAS_OPE_NO,\n" +
                "                        MEAS_PASS_COUNT,\n" +
                "                        TRX_TIME,\n" +
                "                        MEAS_EDC_PLAN_ID,\n" +
                "                        EDC_ITEM_NAME,\n" +
                "                        EDC_MODE,\n" +
                "                        EDC_UOM,\n" +
                "                        DATA_TYPE,\n" +
                "                        ITEM_TYPE,\n" +
                "                        MEAS_TYPE,\n" +
                "                        WAFER_ID,\n" +
                "                        WAFER_POSITION,\n" +
                "                        SITE_POSITION,\n" +
                "                        EDC_ITEM_VAL,\n" +
                "                        SPEC_CHECK_RESULT,\n" +
                "                        ACTION_CODE,\n" +
                "                        SCRN_UP_LIMIT,\n" +
                "                        SPEC_UP_LIMIT,\n" +
                "                        CTRL_UP_LIMIT,\n" +
                "                        EDC_ITEM_TARGET,\n" +
                "                        SCRN_LO_LIMIT,\n" +
                "                        SPEC_LO_LIMIT,\n" +
                "                        CTRL_LO_LIMIT,\n" +
                "                        STORE_TIME,\n" +
                "                        PJ_ID )\n" +
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
                "                ?,\n" +
                "                ?,\n" +
                "                CURRENT_TIMESTAMP,\n" +
                "                ?)",generateID(Infos.OhcdatahsData.class)
                ,hFHCDATAHS_DATA_MEAS_LOT_ID
                , hFHCDATAHS_DATA_MEAS_MAINPD_ID
                , hFHCDATAHS_DATA_MESA_OPE_NO
                , hFHCDATAHS_DATA_MEAS_PASS_COUNT
                , convert(hFHCDATAHS_DATA_CLAIM_TIME)
                , hFHCDATAHS_DATA_MEAS_DCDEF_ID
                , hFHCDATAHS_DATA_DCITEM_NAME
                , hFHCDATAHS_DATA_DCMODE
                , hFHCDATAHS_DATA_DCUNIT
                , hFHCDATAHS_DATA_DATA_TYPE
                , hFHCDATAHS_DATA_ITEM_TYPE
                , hFHCDATAHS_DATA_MEAS_TYPE
                , hFHCDATAHS_DATA_WAFER_ID
                , hFHCDATAHS_DATA_WAFER_POSITION
                , hFHCDATAHS_DATA_SITE_POSITION
                , hFHCDATAHS_DATA_DCITEM_VALUE
                , hFHCDATAHS_DATA_SPECCHK_RESULT
                , hFHCDATAHS_DATA_ACT_CODE
                , hFHCDATAHS_DATA_SCRN_UPPER_LIMIT
                , hFHCDATAHS_DATA_SPEC_UPPER_LIMIT
                , hFHCDATAHS_DATA_CNTL_UPPER_LIMIT
                , hFHCDATAHS_DATA_DCITEM_TARGET
                , hFHCDATAHS_DATA_SCRN_LOWER_LIMIT
                , hFHCDATAHS_DATA_SPEC_LOWER_LIMIT
                , hFHCDATAHS_DATA_CNTL_LOWER_LIMIT
                , hFHCDATAHS_DATA_PRCSJOB_ID);


        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param dcspecID
     * @param dcitem_name
     * @param scrn_upper_limit
     * @param spec_upper_limit
     * @param ctrl_upper_limit
     * @param dcitem_target
     * @param scrn_lower_limit
     * @param spec_lower_limit
     * @param ctrl_lower_limit
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 14:18
     */
    public Response getFRDCSPEC_ITEM(Params.String dcspecID,
                                     Params.String dcitem_name,
                                     Params.Param<Double> scrn_upper_limit,
                                     Params.Param<Double> spec_upper_limit,
                                     Params.Param<Double> ctrl_upper_limit,
                                     Params.Param<Double> dcitem_target,
                                     Params.Param<Double> scrn_lower_limit,
                                     Params.Param<Double> spec_lower_limit,
                                     Params.Param<Double> ctrl_lower_limit ) {
        String  hDCSPEC_ID =null;
        String  hDCITEM_NAME=null;
        Double  hFRDCSPEC_ITEM_SCRN_UPPER_LIMIT=null;
        Double  hFRDCSPEC_ITEM_SPEC_UPPER_LIMIT=null;
        Double  hFRDCSPEC_ITEM_CNTL_UPPER_LIMIT=null;
        Double  hFRDCSPEC_ITEM_DCITEM_TARGET=null;
        Double  hFRDCSPEC_ITEM_SCRN_LOWER_LIMIT=null;
        Double  hFRDCSPEC_ITEM_SPEC_LOWER_LIMIT=null;
        Double  hFRDCSPEC_ITEM_CNTL_LOWER_LIMIT=null;

        hDCSPEC_ID="";
        hDCITEM_NAME="";

        scrn_upper_limit.setValue( 0.0);
        spec_upper_limit.setValue( 0.0);
        ctrl_upper_limit.setValue( 0.0);
        dcitem_target   .setValue( 0.0);
        scrn_lower_limit.setValue( 0.0);
        spec_lower_limit.setValue( 0.0);
        ctrl_lower_limit.setValue( 0.0);

        hFRDCSPEC_ITEM_SCRN_UPPER_LIMIT = 0.0;
        hFRDCSPEC_ITEM_SPEC_UPPER_LIMIT = 0.0;
        hFRDCSPEC_ITEM_CNTL_UPPER_LIMIT = 0.0;
        hFRDCSPEC_ITEM_DCITEM_TARGET    = 0.0;
        hFRDCSPEC_ITEM_SCRN_LOWER_LIMIT = 0.0;
        hFRDCSPEC_ITEM_SPEC_LOWER_LIMIT = 0.0;
        hFRDCSPEC_ITEM_CNTL_LOWER_LIMIT = 0.0;

        hDCSPEC_ID=dcspecID .getValue();
        hDCITEM_NAME=dcitem_name .getValue();

        Object[] one=baseCore.queryOne("SELECT  ITEM.SCRN_UP_LIMIT, ITEM.SPEC_UP_LIMIT, ITEM.CTRL_UP_LIMIT,\n" +
                "                ITEM.SPEC_ITEM_TARGET,\n" +
                "                ITEM.SCRN_LO_LIMIT, ITEM.SPEC_LO_LIMIT, ITEM.CTRL_LO_LIMIT\n" +
                "        FROM    OMEDCSPEC_ITEM ITEM,\n" +
                "                OMEDCSPEC      SPEC\n" +
                "        WHERE   SPEC.EDC_SPEC_ID         = ?\n" +
                "        AND     ITEM.REFKEY    = SPEC.ID\n" +
                "        AND     ITEM.SPEC_ITEM_NAME       = ?",hDCSPEC_ID
                , hDCITEM_NAME);

        if (one!=null) {
            hFRDCSPEC_ITEM_SCRN_UPPER_LIMIT=convertD(one[0]);
            hFRDCSPEC_ITEM_SPEC_UPPER_LIMIT=convertD(one[1]);
            hFRDCSPEC_ITEM_CNTL_UPPER_LIMIT=convertD(one[2]);
            hFRDCSPEC_ITEM_DCITEM_TARGET=convertD(one[3]);
            hFRDCSPEC_ITEM_SCRN_LOWER_LIMIT=convertD(one[4]);
            hFRDCSPEC_ITEM_SPEC_LOWER_LIMIT=convertD(one[5]);
            hFRDCSPEC_ITEM_CNTL_LOWER_LIMIT=convertD(one[6]);
        }


        scrn_upper_limit .setValue(hFRDCSPEC_ITEM_SCRN_UPPER_LIMIT);
        spec_upper_limit .setValue(hFRDCSPEC_ITEM_SPEC_UPPER_LIMIT);
        ctrl_upper_limit .setValue(hFRDCSPEC_ITEM_CNTL_UPPER_LIMIT);
        dcitem_target    .setValue(hFRDCSPEC_ITEM_DCITEM_TARGET);
        scrn_lower_limit .setValue(hFRDCSPEC_ITEM_SCRN_LOWER_LIMIT);
        spec_lower_limit .setValue(hFRDCSPEC_ITEM_SPEC_LOWER_LIMIT);
        ctrl_lower_limit .setValue(hFRDCSPEC_ITEM_CNTL_LOWER_LIMIT);

        Response iRc=returnOK();
        return(iRc);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param poObj
     * @param d_SeqNo
     * @param count
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 14:08
     */
    public Response selectFRPO_DC_ITEMS(String poObj, Params.Param<Integer> d_SeqNo, Params.Param<Integer> count ){
        String hFRPO_DC_ITEMS_d_theTableMarker;
        String hFRPO_DC_ITEMS_d_theTableMarker2;
        int hFRPO_DC_ITEMS_COUNT;
        String hFRPO_PO_OBJ5;

        count .setValue(0);
        hFRPO_DC_ITEMS_COUNT      = 0;
        hFRPO_DC_ITEMS_d_theTableMarker="";
        hFRPO_DC_ITEMS_d_theTableMarker2="";
        hFRPO_PO_OBJ5="";

        hFRPO_DC_ITEMS_d_theTableMarker=String.format(SPConstant.DC_D_THETABLEMARKER, d_SeqNo.getValue() );
        hFRPO_DC_ITEMS_d_theTableMarker2=String.format("%d", d_SeqNo.getValue() );
        hFRPO_PO_OBJ5 = poObj       ;

        int one=baseCore.count("SELECT  count(DC_ITEM.REFKEY)\n" +
                "        FROM OMPROPE_EDC_ITEMS    DC_ITEM,\n" +
                "                OMPROPE             PO\n" +
                "        Where   PO.ID                    = ?\n" +
                "        AND     PO.ID            = DC_ITEM.REFKEY\n" +
                "        AND     (    DC_ITEM.LINK_MARKER     = ?\n" +
                "        OR DC_ITEM.LINK_MARKER     = ?)",hFRPO_PO_OBJ5
                , hFRPO_DC_ITEMS_d_theTableMarker
                , hFRPO_DC_ITEMS_d_theTableMarker2 );

        hFRPO_DC_ITEMS_COUNT=one;


        count.setValue(hFRPO_DC_ITEMS_COUNT );


        if(count.getValue() == 0) {
            Response iRc=returnOK();
            return( iRc ) ;
        }

        List<Object[]> DC_1=baseCore.queryAll("SELECT  DC_ITEM.EDC_ITEM_NAME,    DC_ITEM.WAFER_ID,  DC_ITEM.WAFER_POSITION,\n" +
                "                DC_ITEM.SITE_POSITION,  DC_ITEM.DATA_VAL,  DC_ITEM.EDC_MODE,\n" +
                "                DC_ITEM.EDC_UOM,         DC_ITEM.DATA_TYPE, DC_ITEM.ITEM_TYPE,\n" +
                "                DC_ITEM.SPEC_CHECK_RESULT, DC_ITEM.ACTION_CODE,\n" +
                "                DC_ITEM.MEAS_TYPE,      DC_ITEM.STORE_FLAG\n" +
                "        FROM    OMPROPE_EDC_ITEMS           DC_ITEM,\n" +
                "                OMPROPE                    PO\n" +
                "        WHERE   PO.ID               = ?\n" +
                "        AND     PO.ID       = DC_ITEM.REFKEY\n" +
                "        AND     (   DC_ITEM.LINK_MARKER= ?\n" +
                "        OR DC_ITEM.LINK_MARKER= ?)",hFRPO_PO_OBJ5
                , hFRPO_DC_ITEMS_d_theTableMarker
                , hFRPO_DC_ITEMS_d_theTableMarker2);

        Response iRc=returnOK();
        iRc.setBody(DC_1);

        return(iRc);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param measuredObjrefPO
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 13:28
     */
    public Response selectFRPO_SMPL(String measuredObjrefPO) {
        String hFRPO_PO_OBJ9;

        hFRPO_PO_OBJ9 = measuredObjrefPO;

        List<Object[]> FRPO_SMPL=baseCore.queryAll("select  PA.SAMPLE_WAFER_ID,\n" +
                "                PA.SAMPLING_FLAG,\n" +
                "                PA.PJ_ID,\n" +
                "                PA.PJ_POSITION\n" +
                "        from            OMPROPE         PO\n" +
                "        inner   join    OMPROPE_WAFERJOB    PA on PO.ID = PA.REFKEY\n" +
                "        where   PO.ID = ?\n" +
                "        order   by PA.IDX_NO",hFRPO_PO_OBJ9);


        Response iRc=returnOK();
        iRc.setBody(FRPO_SMPL);

        return iRc;
    }

}
