package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
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
@Repository
@Transactional(rollbackFor = Exception.class)
public class EntityInhibitHistoryService {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param id
     * @return com.fa.cim.dto.Infos.ProcessJobChangeEventRecord
     * @exception
     * @author Ho
     * @date 2019/7/2 17:22
     */
    public Infos.EntityInhibitEventRecord getEventData(String id) {
        String sql="Select * from OMEVRESTRICT where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.EntityInhibitEventRecord theEventData=new Infos.EntityInhibitEventRecord();

        List<Infos.EntityIdentifier> entities=new ArrayList<>();
        theEventData.setEntities(entities);
        ArrayList<Infos.EntityIdentifier> expEntityIdentifiers = new ArrayList<>();
        theEventData.setExpEntities(expEntityIdentifiers);
        List<String> sublottypes=new ArrayList<>();
        theEventData.setSublottypes(sublottypes);
        List<Infos.EntityInhibitReasonDetailInfo> reasonDetailInfos=new ArrayList<>();
        theEventData.setReasonDetailInfos(reasonDetailInfos);
        List<Infos.ExceptionLotRecord> exceptionLots=new ArrayList<>();
        theEventData.setExceptionLots(exceptionLots);

        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            String  refrenceKey = generateID(Infos.Oheninhs.class);
            theEventData.setHistoryID(refrenceKey);
            theEventData.setInhibitID(convert(sqlData.get("RESTRICT_ID")));
            theEventData.setDescription(convert(sqlData.get("DESCRIPTION")));
            theEventData.setStartTimeStamp(convert(sqlData.get("START_TIME")));
            theEventData.setEndTimeStamp(convert(sqlData.get("END_TIME")));
            theEventData.setFunctionRule(convert(sqlData.get("FUNC_RULE")));
            theEventData.setSpecificTool(convertB(sqlData.get("SPECIFIC_TOOL")));
            theEventData.setMemo(convert(sqlData.get("TRX_MEMO")));
            // ----------------------------------------
            // set OHEVENTRESTICT_ENTITY information
            // ----------------------------------------
            sql="SELECT * FROM OMEVRESTRICT_ENTITY WHERE REFKEY=?";
            List<Map> sqlEntities = baseCore.queryAllForMap(sql, id);

            for (Map sqlEntitie:sqlEntities){
                Infos.EntityIdentifier entitie=new Infos.EntityIdentifier();
                entities.add(entitie);

                entitie.setClassName(convert(sqlEntitie.get("ENTITY_TYPE")));
                entitie.setObjectId(objectIdentifier(convert(sqlEntitie.get("ENTITY_ID")),
                        convert(sqlEntitie.get("ENTITY_RKEY"))));
                entitie.setAttrib(convert(sqlEntitie.get("ENTITY_ATTRIB")));
                entitie.setRefrenceKey(refrenceKey);
            }

            // ----------------------------------------
            // set OHEVENTRESTICT_EXPENTITY information
            // ----------------------------------------
            sql = "SELECT * FROM OMEVRESTRICT_EXPENTITY WHERE REFKEY=?";
            List<Map> sqlExpEntities = baseCore.queryAllForMap(sql, id);

            for (Map sqlExpEntitie:sqlExpEntities){
                Infos.EntityIdentifier entitie=new Infos.EntityIdentifier();
                expEntityIdentifiers.add(entitie);

                entitie.setClassName(convert(sqlExpEntitie.get("ENTITY_TYPE")));
                entitie.setObjectId(objectIdentifier(convert(sqlExpEntitie.get("ENTITY_ID")),
                        convert(sqlExpEntitie.get("ENTITY_RKEY"))));
                entitie.setAttrib(convert(sqlExpEntitie.get("ENTITY_ATTRIB")));
                entitie.setRefrenceKey(refrenceKey);
            }

            // ----------------------------------------
            // set OMEVRESTRICT_LOTTP information
            // ----------------------------------------
            sql="SELECT * FROM OMEVRESTRICT_LOTTP WHERE REFKEY=?";
            List<Map> sqlSublottypes = baseCore.queryAllForMap(sql, id);

            for (Map sqlSublottype:sqlSublottypes){
                sublottypes.add(convert(sqlSublottype.get("SUB_LOT_TYPE")));
            }

            theEventData.setReasonCode(convert(sqlData.get("REASON_CODE")));
            theEventData.setReasonDesc(convert(sqlData.get("REASON_DESC")));
            theEventData.setOwnerID(convert(sqlData.get("OWNER_ID")));
            theEventData.setAppliedContext(convert(sqlData.get("RESTRICT_CONTEXT")));

            // ----------------------------------------
            // set OMEVRESTRICT_RSNINFO information
            // ----------------------------------------
            sql="SELECT * FROM OMEVRESTRICT_RSNINFO WHERE REFKEY=?";
            List<Map> sqlReasonDetailInfos = baseCore.queryAllForMap(sql, id);

            for (Map sqlReasonDetailInfo:sqlReasonDetailInfos){
                Infos.EntityInhibitReasonDetailInfo reasonDetailInfo=new Infos.EntityInhibitReasonDetailInfo();
                reasonDetailInfos.add(reasonDetailInfo);

                reasonDetailInfo.setRelatedLotID(convert(sqlReasonDetailInfo.get("LOT_ID")));
                reasonDetailInfo.setRelatedControlJobID(convert(sqlReasonDetailInfo.get("CJ_ID")));
                reasonDetailInfo.setRelatedFabID(convert(sqlReasonDetailInfo.get("FAB_ID")));
                reasonDetailInfo.setRelatedRouteID(convert(sqlReasonDetailInfo.get("MAIN_PROCESS_ID")));
                reasonDetailInfo.setRelatedProcessDefinitionID(convert(sqlReasonDetailInfo.get("STEP_ID")));
                reasonDetailInfo.setRelatedOperationNumber(convert(sqlReasonDetailInfo.get("OPE_NO")));
                reasonDetailInfo.setRelatedOperationPassCount(convert(sqlReasonDetailInfo.get("PASS_COUNT")));

                sql="SELECT * FROM OMEVRESTRICT_RSNINFO_SPC WHERE REFKEY=? AND IDX_NO=?";
                List<Map> sqlRelatedSpcChartInfos=baseCore.queryAllForMap(sql,id,convert(sqlReasonDetailInfo.get("IDX_NO")));

                List<Infos.EntityInhibitSpcChartInfo> relatedSpcChartInfos=new ArrayList<>();
                reasonDetailInfo.setRelatedSpcChartInfos(relatedSpcChartInfos);

                for (Map sqlRelatedSpcChartInfo:sqlRelatedSpcChartInfos){
                    Infos.EntityInhibitSpcChartInfo relatedSpcChartInfo=new Infos.EntityInhibitSpcChartInfo();
                    relatedSpcChartInfos.add(relatedSpcChartInfo);

                    relatedSpcChartInfo.setRelatedSpcDcType(convert(sqlRelatedSpcChartInfo.get("EDC_TYPE")));
                    relatedSpcChartInfo.setRelatedSpcChartGroupID(convert(sqlRelatedSpcChartInfo.get("CHART_GRP_ID")));
                    relatedSpcChartInfo.setRelatedSpcChartID(convert(sqlRelatedSpcChartInfo.get("CHART_ID")));
                    relatedSpcChartInfo.setRelatedSpcChartType(convert(sqlRelatedSpcChartInfo.get("CHART_TYPE")));
                }
            }

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            sql="SELECT * FROM OMEVRESTRICT_EXPLOT WHERE REFKEY=?";
            List<Map> sqlExceptionLots = baseCore.queryAllForMap(sql, id);

            for (Map sqlExceptionLot:sqlExceptionLots){
                Infos.ExceptionLotRecord exceptionLot=new Infos.ExceptionLotRecord();
                exceptionLots.add(exceptionLot);

                exceptionLot.setLotID(objectIdentifier(convert(sqlExceptionLot.get("EXCEPT_LOT_ID")),null));
                exceptionLot.setSingleTriggerFlag(convertB(sqlExceptionLot.get("SINGLE_TRIG_FLAG")));
                /*exceptionLot.setUsedFlag(convert(sqlExceptionLot.get("")));
                exceptionLot.setClaimUserID(convert(sqlExceptionLot.get("")));
                exceptionLot.setClaimTimeStamp(convert(sqlExceptionLot.get("")));
                exceptionLot.setClaimMemo(convert(sqlExceptionLot.get("")));*/
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
     * @date 2019/7/1 11:24
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVRESTRICT_CDA WHERE REFKEY=?";
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
     * @param entityInhibitRecord
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:31
     */
    public Response insertEntityInhibitHistory( Infos.Oheninhs entityInhibitRecord ) {
        String historyID;
        String hFHENINHS_INHIBIT_ID     ;
        String hFHENINHS_INHIBIT_TYPE   ;
        String hFHENINHS_START_TIME     ;
        String hFHENINHS_END_TIME       ;
        String hFHENINHS_REASON_CODE    ;
        String hFHENINHS_REASON_DESC    ;
        String hFHENINHS_DESCRIPTION    ;
        String hFHENINHS_CLAIM_TIME     ;
        Double   hFHENINHS_CLAIM_SHOP_DATE;
        String hFHENINHS_CLAIM_USER_ID  ;
        String hFHENINHS_CLAIM_MEMO     ;
        String hFHENINHS_EVENT_CREATE_TIME ;
        String hFHENINHS_APPLIED_CONTEXT ;
        String hFHENINHS_FUNCTION_RULE  ;
        Boolean hFHENINHS_SPECIFIC_RULE;
        String memo;
        historyID = entityInhibitRecord.getHistoryID();
        hFHENINHS_INHIBIT_ID = entityInhibitRecord.getInhibit_id();
        hFHENINHS_INHIBIT_TYPE = entityInhibitRecord.getInhibit_type();
        hFHENINHS_START_TIME = entityInhibitRecord.getStart_time();
        hFHENINHS_END_TIME = entityInhibitRecord.getEnd_time();
        hFHENINHS_REASON_CODE = entityInhibitRecord.getReason_code();
        hFHENINHS_REASON_DESC = entityInhibitRecord.getReason_desc();
        hFHENINHS_DESCRIPTION = entityInhibitRecord.getDescription();
        hFHENINHS_CLAIM_TIME = entityInhibitRecord.getClaim_time();
        hFHENINHS_CLAIM_SHOP_DATE  = entityInhibitRecord.getClaim_shop_date();
        hFHENINHS_CLAIM_USER_ID = entityInhibitRecord.getClaim_user_id();
        hFHENINHS_CLAIM_MEMO = entityInhibitRecord.getClaim_memo();
        hFHENINHS_EVENT_CREATE_TIME = entityInhibitRecord.getEvent_create_time ();
        hFHENINHS_APPLIED_CONTEXT = entityInhibitRecord.getApplied_context ();
        hFHENINHS_FUNCTION_RULE = entityInhibitRecord.getFunction_rule();
        hFHENINHS_SPECIFIC_RULE = entityInhibitRecord.getSpecific_tool();

        baseCore.insert("INSERT INTO OHRESTRICT\n" +
                "            (ID,  RESTRICT_ID,\n" +
                "                    TASK_TYPE,\n" +
                "                    START_TIME,\n" +
                "                    END_TIME,\n" +
                "                    REASON_CODE,\n" +
                "                    REASON_DESC,\n" +
                "                    DESCRIPTION,\n" +
                "                    TRX_TIME,\n" +
                "                    TRX_WORK_DATE,\n" +
                "                    TRX_USER_ID,\n" +
                "                    TRX_MEMO,\n" +
                "                    EVENT_CREATE_TIME,\n" +
                "                    RESTRICT_CONTEXT,\n" +
                "                    FUNC_RULE,\n" +
                "                    SPECIFIC_TOOL,\n" +
                "                    STORE_TIME )\n" +
                "        Values\n" +
                "                (?,  ?,\n" +
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
                "            CURRENT_TIMESTAMP       )",historyID
                ,hFHENINHS_INHIBIT_ID
                ,hFHENINHS_INHIBIT_TYPE
                ,convert(hFHENINHS_START_TIME)
                ,convert(hFHENINHS_END_TIME)
                ,hFHENINHS_REASON_CODE
                ,hFHENINHS_REASON_DESC
                ,hFHENINHS_DESCRIPTION
                ,convert(hFHENINHS_CLAIM_TIME)
                ,hFHENINHS_CLAIM_SHOP_DATE
                ,hFHENINHS_CLAIM_USER_ID
                ,hFHENINHS_CLAIM_MEMO
                ,convert(hFHENINHS_EVENT_CREATE_TIME)
                ,hFHENINHS_APPLIED_CONTEXT
                ,hFHENINHS_FUNCTION_RULE
                ,hFHENINHS_SPECIFIC_RULE);

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param entityInhibitRecord_entity
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:40
     */
    public Response insertEntityInhibitHistory_entity( Infos.Oheninhs_entity entityInhibitRecord_entity ) {
        String hFHENINHS_ENTITY_INHIBIT_ID;
        String hFHENINHS_ENTITY_CLAIM_TIME;
        String hFHENINHS_ENTITY_CLASS_NAME;
        String hFHENINHS_ENTITY_ENTITY_ID;
        String hFHENINHS_ENTITY_ATTRIB;
        String refrenceKey;

        hFHENINHS_ENTITY_INHIBIT_ID = entityInhibitRecord_entity.getInhibit_id();
        hFHENINHS_ENTITY_CLAIM_TIME = entityInhibitRecord_entity.getClaim_time();
        hFHENINHS_ENTITY_CLASS_NAME = entityInhibitRecord_entity.getClass_name();
        hFHENINHS_ENTITY_ENTITY_ID  = entityInhibitRecord_entity.getEntity_id();
        hFHENINHS_ENTITY_ATTRIB     = entityInhibitRecord_entity.getAttrib();
        refrenceKey = entityInhibitRecord_entity.getRefrenceKey();

        baseCore.insert("INSERT INTO OHRESTRICT_ENTITY\n" +
                "            ( ID, RESTRICT_ID,\n" +
                "                    TRX_TIME,\n" +
                "                    ENTITY_TYPE,\n" +
                "                    ENTITY_ID,\n" +
                "                    ENTITY_ATTRIB,\n" +
                "                    REFKEY )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ? )",generateID(Infos.Oheninhs_entity.class)
                ,hFHENINHS_ENTITY_INHIBIT_ID
                ,convert(hFHENINHS_ENTITY_CLAIM_TIME)
                ,hFHENINHS_ENTITY_CLASS_NAME
                ,hFHENINHS_ENTITY_ENTITY_ID
                ,hFHENINHS_ENTITY_ATTRIB
                ,refrenceKey );


        return( returnOK() );
    }

    /**
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/9 14:51                       AOKI              Create
    * @author AOKI
    * @date 2021/6/9 14:51
    * @param entityInhibitRecord_entity
    * @return com.fa.cim.dto.Response
    */
    public Response insertExpEntityInhibitHistory_entity( Infos.Oheninhs_entity entityInhibitRecord_entity ) {
        String hFHENINHS_ENTITY_INHIBIT_ID;
        String hFHENINHS_ENTITY_CLAIM_TIME;
        String hFHENINHS_ENTITY_CLASS_NAME;
        String hFHENINHS_ENTITY_ENTITY_ID;
        String hFHENINHS_ENTITY_ATTRIB;
        String refrenceKey;

        refrenceKey = entityInhibitRecord_entity.getRefrenceKey();
        hFHENINHS_ENTITY_INHIBIT_ID = entityInhibitRecord_entity.getInhibit_id();
        hFHENINHS_ENTITY_CLAIM_TIME = entityInhibitRecord_entity.getClaim_time();
        hFHENINHS_ENTITY_CLASS_NAME = entityInhibitRecord_entity.getClass_name();
        hFHENINHS_ENTITY_ENTITY_ID  = entityInhibitRecord_entity.getEntity_id();
        hFHENINHS_ENTITY_ATTRIB     = entityInhibitRecord_entity.getAttrib();

        baseCore.insert("INSERT INTO OHRESTRICT_EXPENTITY\n" +
                        "            ( ID, RESTRICT_ID,\n" +
                        "                    TRX_TIME,\n" +
                        "                    ENTITY_TYPE,\n" +
                        "                    ENTITY_ID,\n" +
                        "                    ENTITY_ATTRIB," +
                        "                    REFKEY)\n" +
                        "        Values\n" +
                        "                ( ?, ?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "                ?,\n" +
                        "                ?,\n"+
                        "                ? )",generateID(Infos.Oheninhs_entity.class)
                ,hFHENINHS_ENTITY_INHIBIT_ID
                ,convert(hFHENINHS_ENTITY_CLAIM_TIME)
                ,hFHENINHS_ENTITY_CLASS_NAME
                ,hFHENINHS_ENTITY_ENTITY_ID
                ,hFHENINHS_ENTITY_ATTRIB
                ,refrenceKey);


        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param entityInhibitRecord_sublot
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:45
     */
    public Response insertEntityInhibitHistory_sublot( Infos.Oheninhs_sublot entityInhibitRecord_sublot ) {
        String hFHENINHS_SUBLOT_INHIBIT_ID;
        String hFHENINHS_SUBLOT_CLAIM_TIME;
        String hFHENINHS_SUBLOT_SUB_LOT_TYPE;

        hFHENINHS_SUBLOT_INHIBIT_ID = entityInhibitRecord_sublot.getInhibit_id();
        hFHENINHS_SUBLOT_CLAIM_TIME = entityInhibitRecord_sublot.getClaim_time();
        hFHENINHS_SUBLOT_SUB_LOT_TYPE = entityInhibitRecord_sublot.getSub_lot_type();

        baseCore.insert("INSERT INTO OHRESTRICT_LOTTP\n" +
                "            ( ID, RESTRICT_ID,\n" +
                "                    TRX_TIME,\n" +
                "                    LOT_TYPE     )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
                "                ?,\n" +
                "                ?)",generateID(Infos.Oheninhs_sublot.class)
                ,hFHENINHS_SUBLOT_INHIBIT_ID
                ,convert(hFHENINHS_SUBLOT_CLAIM_TIME)
                ,hFHENINHS_SUBLOT_SUB_LOT_TYPE  );

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param entityInhibitRecord_rsninfo
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:51
     */
    public Response insertEntityInhibitHistory_rsninfo( Infos.Oheninhs_rsninfo entityInhibitRecord_rsninfo ) {
        String  hFHENINHS_RSNINFO_INHIBIT_ID;
        String  hFHENINHS_RSNINFO_LOT_ID;
        String  hFHENINHS_RSNINFO_CTRLJOB_ID;
        String  hFHENINHS_RSNINFO_FAB_ID;
        String  hFHENINHS_RSNINFO_MAINPD_ID;
        String  hFHENINHS_RSNINFO_PD_ID;
        String  hFHENINHS_RSNINFO_OPE_NO;
        String  hFHENINHS_RSNINFO_PASS_COUNT;
        String  hFHENINHS_RSNINFO_CLAIM_TIME;

        hFHENINHS_RSNINFO_INHIBIT_ID = entityInhibitRecord_rsninfo.getInhibit_id();
        hFHENINHS_RSNINFO_LOT_ID = entityInhibitRecord_rsninfo.getLot_id();
        hFHENINHS_RSNINFO_CTRLJOB_ID = entityInhibitRecord_rsninfo.getCtrljob_id();
        hFHENINHS_RSNINFO_FAB_ID = entityInhibitRecord_rsninfo.getFab_id();
        hFHENINHS_RSNINFO_MAINPD_ID = entityInhibitRecord_rsninfo.getMainpd_id();
        hFHENINHS_RSNINFO_PD_ID = entityInhibitRecord_rsninfo.getPd_id();
        hFHENINHS_RSNINFO_OPE_NO = entityInhibitRecord_rsninfo.getOpe_no();
        hFHENINHS_RSNINFO_PASS_COUNT = entityInhibitRecord_rsninfo.getPass_count();
        hFHENINHS_RSNINFO_CLAIM_TIME = entityInhibitRecord_rsninfo.getClaim_time();

        baseCore.insert("INSERT INTO OHRESTRICT_RSNINFO\n" +
                "            ( ID, RESTRICT_ID,\n" +
                "                    TRX_TIME, -- PSN000087754\n" +
                "                    LOT_ID,\n" +
                "                    CJ_ID,\n" +
                "                    FAB_ID,\n" +
                "                    MAIN_PROCESS_ID,\n" +
                "                    STEP_ID,\n" +
                "                    OPE_NO,\n" +
                "                    PASS_COUNT        )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?)",generateID(Infos.Oheninhs_rsninfo.class)
                ,hFHENINHS_RSNINFO_INHIBIT_ID
                ,convert(hFHENINHS_RSNINFO_CLAIM_TIME)
                ,hFHENINHS_RSNINFO_LOT_ID
                ,hFHENINHS_RSNINFO_CTRLJOB_ID
                ,hFHENINHS_RSNINFO_FAB_ID
                ,hFHENINHS_RSNINFO_MAINPD_ID
                ,hFHENINHS_RSNINFO_PD_ID
                ,hFHENINHS_RSNINFO_OPE_NO
                ,hFHENINHS_RSNINFO_PASS_COUNT  );

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param entityInhibitRecord_rsninfo_spc
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 14:58
     */
    public Response insertEntityInhibitHistory_rsninfo_spc( Infos.Oheninhs_rsninfo_spc entityInhibitRecord_rsninfo_spc ) {
        String hFHENINHS_RSNINFO_SPC_INHIBIT_ID;
        String hFHENINHS_RSNINFO_SPC_DC_TYPE;
        String hFHENINHS_RSNINFO_SPC_CHART_GRP_ID;
        String hFHENINHS_RSNINFO_SPC_CHART_ID;
        String hFHENINHS_RSNINFO_SPC_CHART_TYPE;
        String hFHENINHS_RSNINFO_SPC_CLAIM_TIME;

        hFHENINHS_RSNINFO_SPC_INHIBIT_ID = entityInhibitRecord_rsninfo_spc.getInhibit_id();
        hFHENINHS_RSNINFO_SPC_DC_TYPE = entityInhibitRecord_rsninfo_spc.getDc_type();
        hFHENINHS_RSNINFO_SPC_CHART_GRP_ID = entityInhibitRecord_rsninfo_spc.getChart_grp_id();
        hFHENINHS_RSNINFO_SPC_CHART_ID = entityInhibitRecord_rsninfo_spc.getChart_id();
        hFHENINHS_RSNINFO_SPC_CHART_TYPE = entityInhibitRecord_rsninfo_spc.getChart_type();
        hFHENINHS_RSNINFO_SPC_CLAIM_TIME = entityInhibitRecord_rsninfo_spc.getClaim_time();

        baseCore.insert("INSERT INTO OHRESTRICT_RSNINFO_SPC\n" +
                "            ( ID, RESTRICT_ID,\n" +
                "                    TRX_TIME,\n" +
                "                    EDC_TYPE,\n" +
                "                    CHART_GRP_ID,\n" +
                "                    CHART_ID,\n" +
                "                    CHART_TYPE        )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?)",generateID(Infos.Oheninhs_rsninfo_spc.class)
                ,hFHENINHS_RSNINFO_SPC_INHIBIT_ID
                ,convert(hFHENINHS_RSNINFO_SPC_CLAIM_TIME)
                ,hFHENINHS_RSNINFO_SPC_DC_TYPE
                ,hFHENINHS_RSNINFO_SPC_CHART_GRP_ID
                ,hFHENINHS_RSNINFO_SPC_CHART_ID
                ,hFHENINHS_RSNINFO_SPC_CHART_TYPE  );

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param entityInhibitRecord__explot
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/4 15:04
     */
    public Response insertEntityInhibitHistory_explot( Infos.Oheninhs_explot entityInhibitRecord__explot ) {
        String hFHENINHS_EXPLOT_INHIBIT_ID;
        String hFHENINHS_EXPLOT_EXCEPT_LOT_ID;
        Boolean hFHENINHS_EXPLOT_SINGLE_TRIG_FLAG;
        String hFHENINHS_EXPLOT_CLAIM_TIME;

        hFHENINHS_EXPLOT_INHIBIT_ID = entityInhibitRecord__explot.getInhibit_id();
        hFHENINHS_EXPLOT_EXCEPT_LOT_ID = entityInhibitRecord__explot.getLot_id();
        hFHENINHS_EXPLOT_SINGLE_TRIG_FLAG  = entityInhibitRecord__explot.getSingle_trig_flag();
        hFHENINHS_EXPLOT_CLAIM_TIME = entityInhibitRecord__explot.getClaim_time();

        baseCore.insert("INSERT INTO OHRESTRICT_EXPLOT\n" +
                "            ( ID, RESTRICT_ID,\n" +
                "                    TRX_TIME,\n" +
                "                    EDC_TYPE,\n" +
                "                    SINGLE_TRIG_FLAG  )\n" +
                "        Values\n" +
                "                ( ?, ?,\n" +
                "                ?,\n" +
                "                ?,\n" +
                "                ?)",generateID(Infos.Oheninhs_explot.class)
                ,hFHENINHS_EXPLOT_INHIBIT_ID
                ,convert(hFHENINHS_EXPLOT_CLAIM_TIME)
                ,hFHENINHS_EXPLOT_EXCEPT_LOT_ID
                ,hFHENINHS_EXPLOT_SINGLE_TRIG_FLAG  );

        return( returnOK() );
    }

}
