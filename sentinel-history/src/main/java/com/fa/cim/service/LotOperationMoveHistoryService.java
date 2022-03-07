package com.fa.cim.service;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
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
 * @date 2019/6/6 10:34
 */
@Repository
//@Transactional(rollbackFor = Exception.class)
public class LotOperationMoveHistoryService {

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
     * @return com.fa.cim.dto.Infos.LotOperationMoveEventRecord
     * @exception
     * @author Ho
     * @date 2019/6/6 10:34
     */
    public Infos.LotOperationMoveEventRecord getEventData(String id) {
        String sql="Select * from OMEVMVOP where id=?";
        List<Map> sqlDatas = baseCore.queryAllForMap(sql, id);
        Infos.LotOperationMoveEventRecord theEventData=new Infos.LotOperationMoveEventRecord();
        Infos.LotEventData lotData=new Infos.LotEventData();
        theEventData.setLotData(lotData);
        Infos.ProcessOperationEventData oldCurrentPOData=new Infos.ProcessOperationEventData();
        theEventData.setOldCurrentPOData(oldCurrentPOData);
        List<String> fixtureIDs = new ArrayList<>();
        theEventData.setFixtureIDs(fixtureIDs);
        List<Infos.WaferPassCountEventData> processWafers = new ArrayList<>();
        theEventData.setProcessWafers(processWafers);
        List<Infos.RecipeParmEventData> recipeParameters=new ArrayList<>();
        theEventData.setRecipeParameters(recipeParameters);
        List<String> reticleIDs=new ArrayList<>();
        theEventData.setReticleIDs(reticleIDs);
        Infos.EventData eventCommon = new Infos.EventData();
        theEventData.setEventCommon(eventCommon);
        for (Map<String,Object> sqlData:sqlDatas) {
            lotData.setLotID(convert(sqlData.get("LOT_ID")));
            lotData.setLotType(convert(sqlData.get("LOT_TYPE")));
            lotData.setCassetteID(convert(sqlData.get("CARRIER_ID")));
            lotData.setLotStatus(convert(sqlData.get("LOT_STATUS")));
            lotData.setCustomerID(convert(sqlData.get("CUSTOMER_ID")));
            lotData.setPriorityClass(convertL(sqlData.get("LOT_PRIORITY")));
            lotData.setProductID(convert(sqlData.get("PROD_ID")));
            lotData.setOriginalWaferQuantity(convertI(sqlData.get("ORIGINAL_QTY")));
            lotData.setCurrentWaferQuantity(convertI(sqlData.get("CUR_QTY")));
            lotData.setProductWaferQuantity(convertI(sqlData.get("PROD_QTY")));
            lotData.setControlWaferQuantity(convertI(sqlData.get("NPW_QTY")));
            lotData.setHoldState(convert(sqlData.get("LOT_HOLD_STATE")));
            lotData.setBankID(convert(sqlData.get("BANK_ID")));
            lotData.setRouteID(convert(sqlData.get("PROCESS_ID")));
            lotData.setOperationNumber(convert(sqlData.get("OPE_NO")));
            lotData.setOperationID(convert(sqlData.get("STEP_ID")));
            lotData.setOperationPassCount(convertI(sqlData.get("PASS_COUNT")));
            lotData.setObjrefPOS(convert(sqlData.get("PRSS_RKEY")));
            lotData.setWaferHistoryTimeStamp(convert(sqlData.get("WAFER_HIS_TIME")));
            lotData.setObjrefPO(convert(sqlData.get("PROPE_RKEY")));
            lotData.setObjrefMainPF(convert(sqlData.get("MROUTE_PRF_RKEY")));
            lotData.setObjrefModulePOS(convert(sqlData.get("ROUTE_PRSS_RKEY")));

            theEventData.setEquipmentID(convert(sqlData.get("EQP_ID")));
            theEventData.setOperationMode(convert(sqlData.get("OPE_MODE")));
            theEventData.setLogicalRecipeID(convert(sqlData.get("LRCP_ID")));
            theEventData.setMachineRecipeID(convert(sqlData.get("MRCP_ID")));
            theEventData.setPhysicalRecipeID(convert(sqlData.get("PRCP_ID")));
            theEventData.setPreviousOperationID(convert(sqlData.get("PREV_STEP_ID")));
            sql="select * from OMEVMVOP_RTCL where refkey=?";
            List<Map> sqlReticles=baseCore.queryAllForMap(sql,id);

            for (Map sqlReticle: sqlReticles) {
                reticleIDs.add(convert(sqlReticle.get("RTCL_ID")));
            }

            sql="select * from OMEVMVOP_FIXT where refkey=?";
            List<Map> sqlFixtures=baseCore.queryAllForMap(sql,id);

            for (Map sqlFixture:sqlFixtures) {
                fixtureIDs.add(convert(sqlFixture.get("FIXTURE_ID")));
            }

            sql="select * from OMEVMVOP_RPARAM where refkey=?";
            List<Map> sqlRecipeParameters=baseCore.queryAllForMap(sql,id);

            for (Map sqlRecipeParameter:sqlRecipeParameters) {
                Infos.RecipeParmEventData recipeParameter=new Infos.RecipeParmEventData();
                recipeParameters.add(recipeParameter);
                recipeParameter.setParameterName(convert(sqlRecipeParameter.get("RPARAM_NAME")));
                recipeParameter.setParameterValue(convert(sqlRecipeParameter.get("RPARAM_VAL")));
            }

            theEventData.setPreviousRouteID(convert(sqlData.get("PREV_PROCESS_ID")));
            theEventData.setPreviousOperationNumber(convert(sqlData.get("PREV_OPE_NO")));
            theEventData.setPreviousOperationPassCount(convertL(sqlData.get("PREV_PASS_COUNT")));
            theEventData.setPreviousObjrefPOS(convert(sqlData.get("PREV_PRSS_RKEY")));
            theEventData.setPreviousObjrefMainPF(convert(sqlData.get("PREV_MROUTE_PRF_RKEY")));
            theEventData.setPreviousObjrefModulePOS(convert(sqlData.get("PREV_ROUTE_PRSS_RKEY")));

            oldCurrentPOData.setObjrefPOS(convert(sqlData.get("OLD_PRSS_RKEY")));
            oldCurrentPOData.setObjrefMainPF(convert(sqlData.get("OLD_MROUTE_PRF_RKEY")));
            oldCurrentPOData.setObjrefModulePOS(convert(sqlData.get("OLD_ROUTE_PRSS_RKEY")));
            oldCurrentPOData.setRouteID(convert(sqlData.get("OLD_PROCESS_ID")));
            oldCurrentPOData.setOperationNumber(convert(sqlData.get("OLD_OPE_NO")));
            oldCurrentPOData.setOperationID(convert(sqlData.get("OLD_STEP_ID")));
            oldCurrentPOData.setOperationPassCount(convertL(sqlData.get("OLD_PASS_COUNT")));
            oldCurrentPOData.setObjrefPO(convert(sqlData.get("OLD_PROPE_RKEY")));

            theEventData.setBatchID(convert(sqlData.get("FLOWB_ID")));
            theEventData.setControlJobID(convert(sqlData.get("CJ_ID")));
            theEventData.setLocateBackFlag(convertB(sqlData.get("SKIP_BACK")));
            theEventData.setTestCriteriaResult(convertB(sqlData.get("TEST_CRITERIA")));

            eventCommon.setTransactionID(convert(sqlData.get("TRX_ID")));
            eventCommon.setEventTimeStamp(convert(sqlData.get("EVENT_TIME")));

            eventCommon.setUserID(convert(sqlData.get("TRX_USER_ID")));
            eventCommon.setEventMemo(convert(sqlData.get("TRX_MEMO")));
            eventCommon.setEventCreationTimeStamp(convert(sqlData.get("EVENT_CREATE_TIME")));

            sql="Select * from OMEVMVOP_PASSCNT where REFKEY=?";
            List<Map> sqlProcessWafers=baseCore.queryAllForMap(sql,id);

            for (Map sqlWafer:sqlProcessWafers) {
                Infos.WaferPassCountEventData processWafer = new Infos.WaferPassCountEventData();
                processWafers.add(processWafer);
                processWafer.setWaferID(convert(sqlWafer.get("WAFER_ID")));
                processWafer.setPreviousPassCount(convertL(sqlWafer.get("PREV_PASS_COUNT")));
                processWafer.setPassCount(convertL(sqlWafer.get("PASS_COUNT")));
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
     * @date 2019/6/4 13:37
     */
    public List<Infos.UserDataSet> allUserDataSets(String refKey) {
        String sql="SELECT * FROM OMEVMVOP_CDA WHERE REFKEY=?";
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
     * @param tableName
     * @param refKey
     * @return void
     * @exception
     * @author Ho
     * @date 2019/6/4 11:25
     */
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

}
