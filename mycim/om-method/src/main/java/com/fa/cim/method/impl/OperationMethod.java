package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IOperationMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * OperationCompImpl .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/30        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/7/30 22:47
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class OperationMethod implements IOperationMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strOperationHistoryFillInTxPLQ008DRIn
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.OperationHistoryFillInTxPLQ008DROut>
     * @exception
     * @author Ho
     * @date 2019/4/25 14:25
     */
    @Override
    public Infos.OperationHistoryFillInTxPLQ008DROut operationHistoryFillInTxPLQ008DR(Infos.ObjCommon strObjCommonIn, Infos.OperationHistoryFillInTxPLQ008DRIn strOperationHistoryFillInTxPLQ008DRIn) {
        ObjectIdentifier lotID          = strOperationHistoryFillInTxPLQ008DRIn.getLotID();
        ObjectIdentifier routeID        = strOperationHistoryFillInTxPLQ008DRIn.getRouteID();
        ObjectIdentifier operationID    = strOperationHistoryFillInTxPLQ008DRIn.getOperationID();
        String operationNumber           = strOperationHistoryFillInTxPLQ008DRIn.getOperationNumber();
        String operationPass             = strOperationHistoryFillInTxPLQ008DRIn.getOperationPass();
        String operationCategory         = strOperationHistoryFillInTxPLQ008DRIn.getOperationCategory();
        Boolean pinPointFlag             = strOperationHistoryFillInTxPLQ008DRIn.getPinPointFlag();

        //add fromTimeStamp and toTimeStamp for runcard history
        String fromTimeStamp = strOperationHistoryFillInTxPLQ008DRIn.getFromTimeStamp();
        String toTimeStamp = strOperationHistoryFillInTxPLQ008DRIn.getToTimeStamp();

        String lvhFHOPEHSLOT_ID = lotID.getValue();
        String lvhFHOPEHSMAINPD_ID = routeID.getValue();
        String lvhFHOPEHSOPE_NO = operationNumber;
        Long lvhFHOPEHSOPE_PASS_COUNT = CimNumberUtils.longValue(operationPass);
        String lvhFHOPEHSMOVE_TYPE = BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION;
        String lvhFHOPEHSMOVE_TYPE_1 = BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE;


        Long  nReticleSeqLen =10L ;

        String theReticleSeqLen = StandardProperties.OM_RETICLE_EXTLEN_FOR_OPE_HISTORY_INQ.getValue();
        if( 0 < CimStringUtils.length(theReticleSeqLen) ) {
            nReticleSeqLen = CimLongUtils.longValue(theReticleSeqLen);
        }

        Long  nFixtureSeqLen =10L ;

        String theFixtureSeqLen = StandardProperties.OM_FIXTURE_EXTLEN_FOR_OPE_HISTORY_INQ.getValue();
        if( 0 < CimStringUtils.length(theFixtureSeqLen) ) {
            nFixtureSeqLen = CimLongUtils.longValue(theFixtureSeqLen);
        }

        Long  nRCPParmSeqLen =10L;

        String theRCPParmSeqLen = StandardProperties.OM_OPEHIST_RCPPARM_EXTLEN_FOR_INQ.getValue();
        if( 0 < CimStringUtils.length(theRCPParmSeqLen) ) {
            nRCPParmSeqLen = CimLongUtils.longValue(theRCPParmSeqLen);
        }

        Long lenOpeHisMax = StandardProperties.OM_OPEHIST_MAX_COUNT_FOR_INQ.getLongValue();

        if( lenOpeHisMax < 100 ) {
            lenOpeHisMax = 100L;
        }

        String sql;
        Infos.OperationHistoryFillInTxPLQ008DROut strOperationHistoryFillInTxPLQ008DROut=new Infos.OperationHistoryFillInTxPLQ008DROut();
        List<Infos.OperationHisInfo> strOperationHisInfo=new ArrayList<>();
        strOperationHistoryFillInTxPLQ008DROut.setStrOperationHisInfo(strOperationHisInfo);
        if(CimBooleanUtils.isTrue(pinPointFlag)) {

            sql="select\n" +
                    "                    LOT_ID,\n" +
                    "                    LOT_TYPE,\n" +
                    "                    CARRIER_ID,\n" +
                    "                    CARRIER_CATEGORY,\n" +
                    "                    PROCESS_ID,\n" +
                    "                    OPE_NO,\n" +
                    "                    STEP_ID,\n" +
                    "            STEP_TYPE,\n" +
                    "            OPE_PASS_COUNT,\n" +
                    "                    STEP_NAME,\n" +
                    "                    HOLD_STATE,\n" +
                    "                    TRX_TIME,\n" +
                    "                    TRX_USER_ID,\n" +
                    "                    MOVE_TYPE,\n" +
                    "                    OPE_CATEGORY,\n" +
                    "                    PROD_TYPE,\n" +
                    "                    TEST_TYPE,\n" +
                    "                    MFG_LAYER,\n" +
                    "                    LOT_PRIORITY,\n" +
                    "                    PREV_PROD_ID,\n" +
                    "                    PROD_ID,\n" +
                    "                    PRODFMLY_ID,\n" +
                    "                    TECH_ID,\n" +
                    "                    CUSTOMER_ID,\n" +
                    "                    CUSTPROD_ID,\n" +
                    "                    MFG_ORDER_NO,\n" +
                    "                    STAGE_ID,\n" +
                    "                    STAGE_GRP_ID,\n" +
                    "                    PHOTO_LAYER,\n" +
                    "                    LOCATION_ID,\n" +
                    "                    BAY_ID,\n" +
                    "                    EQP_ID,\n" +
                    "                    EQP_NAME,\n" +
                    "                    OPE_MODE,\n" +
                    "                    LRCP_ID,\n" +
                    "                    MRCP_ID,\n" +
                    "                    PRCP_ID,\n" +
                    "                    RETICLE_COUNT,\n" +
                    "                    FIXTURE_COUNT,\n" +
                    "                    RPARAM_COUNT,\n" +
                    "                    INITIAL_HOLD_FLAG,\n" +
                    "                    LAST_HOLD_REL_FLAG,\n" +
                    "                    HOLD_TRX_TIME,\n" +
                    "                    HOLD_USER_ID,\n" +
                    "                    HOLD_TYPE,\n" +
                    "                    HOLD_REASON_CODE,\n" +
                    "                    HOLD_REASON_DESC,\n" +
                    "                    REASON_CODE,\n" +
                    "                    REASON_DESC,\n" +
                    "                    BANK_ID,\n" +
                    "                    PREV_BANK_ID,\n" +
                    "                    PREV_PROCESS_ID,\n" +
                    "                    PREV_OPE_NO,\n" +
                    "                    PREV_STEP_ID,\n" +
                    "                    PREV_STEP_TYPE,\n" +
                    "            PREV_STEP_NAME,\n" +
                    "                    PREV_PASS_COUNT,\n" +
                    "                    PREV_STAGE_ID,\n" +
                    "                    PREV_STAGE_GRP_ID,\n" +
                    "                    PREV_PHOTO_LAYER,\n" +
                    "                    FLOWB_ID,\n" +
                    "                    CJ_ID,\n" +
                    "                    REWORK_COUNT,\n" +
                    "                    ORIG_WAFER_QTY,\n" +
                    "                    CUR_WAFER_QTY,\n" +
                    "                    PROD_WAFER_QTY,\n" +
                    "                    NPW_WAFER_QTY,\n" +
                    "                    CLAIM_PROD_QTY,\n" +
                    "                    TRX_NPW_QTY,\n" +
                    "                    TOTAL_GOOD_UNIT,\n" +
                    "                    TOTAL_FAIL_UNIT,\n" +
                    "                    LOT_OWNER_ID,\n" +
                    "                    PLAN_END_TIME,\n" +
                    "                    WAFER_HIS_TIME,\n" +
                    "                    CRITERIA_FLAG,\n" +
                    "                    TRX_MEMO,\n" +
                    "                    RPARAM_CHG_TYPE,     \n" +
                    "            STORE_TIME,\n" +
                    "                    RELATED_LOT_ID,        \n" +
                    "            BOND_GRP_ID,             \n" +
                    "            DPT_NAME_PLATE,            \n" +
                    "            MON_GRP_ID             \n" +
                    "            from  OHLOTOPE\n" +
                    "            where\n" +
                    "            LOT_ID             = ?             and\n" +
                    "            ( ( PROCESS_ID          = ?          and\n" +
                    "            OPE_NO             = ?             and\n" +
                    "            OPE_PASS_COUNT     = ? )\n" +
                    "            or\n" +
                    "                    ( PREV_PROCESS_ID     = ?          and\n" +
                    "            PREV_OPE_NO        = ?             and\n" +
                    "            PREV_PASS_COUNT    = ?     and\n" +
                    "            (  MOVE_TYPE = ? or\n" +
                    "            MOVE_TYPE = ?        ) ) )\n";
            //add fromTimeStamp and toTimeStamp for history query
            String sqlTemp = "";
            if (CimStringUtils.isNotEmpty( fromTimeStamp )) {
                sqlTemp = String.format(" AND EVENT_CREATE_TIME >= TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", fromTimeStamp);
                sql += sqlTemp ;
            }
            if (CimStringUtils.isNotEmpty( toTimeStamp )) {
                sqlTemp = String.format(" AND EVENT_CREATE_TIME <= TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", toTimeStamp);
                sql += sqlTemp ;
            }

            sql += "  order by TRX_TIME, EVENT_CREATE_TIME, STORE_TIME";

            List<Object[]> V11__160 = cimJpaRepository.query(sql, lvhFHOPEHSLOT_ID, lvhFHOPEHSMAINPD_ID, lvhFHOPEHSOPE_NO, lvhFHOPEHSOPE_PASS_COUNT, lvhFHOPEHSMAINPD_ID,
                    lvhFHOPEHSOPE_NO, lvhFHOPEHSOPE_PASS_COUNT, lvhFHOPEHSMOVE_TYPE, lvhFHOPEHSMOVE_TYPE_1);

            Long t_len1 = StandardProperties.OM_OPEHIST_EXTLEN_FOR_INQ_WITH_PIN_POINT.getLongValue();
            Long count1 = 0L;

            for (Object[] obj:V11__160) {
                int i = 0;
                Object hFHOPEHSLOT_ID =obj[i++];
                Object hFHOPEHSLOT_TYPE =obj[i++];
                Object hFHOPEHSCAST_ID =obj[i++];
                Object hFHOPEHSCAST_CATEGORY =obj[i++];
                Object hFHOPEHSMAINPD_ID =obj[i++];
                Object hFHOPEHSOPE_NO =obj[i++];
                Object hFHOPEHSPD_ID =obj[i++];
                Object hFHOPEHSPD_TYPE =obj[i++];
                Object hFHOPEHSOPE_PASS_COUNT =obj[i++];
                Object hFHOPEHSPD_NAME =obj[i++];
                Object hFHOPEHSHOLD_STATE =obj[i++];
                Object hFHOPEHSCLAIM_TIME =obj[i++];
                Object hFHOPEHSCLAIM_USER_ID =obj[i++];
                Object hFHOPEHSMOVE_TYPE = obj[i++];
                Object hFHOPEHSOPE_CATEGORY = obj[i++];
                Object hFHOPEHSPROD_TYPE = obj[i++];
                Object hFHOPEHSTEST_TYPE = obj[i++];
                Object hFHOPEHSMFG_LAYER = obj[i++];
                Object hFHOPEHSEXT_PRIORITY = "";
                Object hFHOPEHSPRIORITY_CLASS = obj[i++];
                Object hFHOPEHSPREV_PRODSPEC_ID = obj[i++];
                Object hFHOPEHSPRODSPEC_ID = obj[i++];
                Object hFHOPEHSPRODGRP_ID = obj[i++];
                Object hFHOPEHSTECH_ID = obj[i++];
                Object hFHOPEHSCUSTOMER_ID = obj[i++];
                Object hFHOPEHSCUSTPROD_ID = obj[i++];
                Object hFHOPEHSORDER_NO = obj[i++];
                Object hFHOPEHSSTAGE_ID = obj[i++];
                Object hFHOPEHSSTAGEGRP_ID = obj[i++];
                Object hFHOPEHSPHOTO_LAYER = obj[i++];
                Object hFHOPEHSLOCATION_ID = obj[i++];
                Object hFHOPEHSAREA_ID = obj[i++];
                Object hFHOPEHSEQP_ID = obj[i++];
                Object hFHOPEHSEQP_NAME = obj[i++];
                Object hFHOPEHSOPE_MODE = obj[i++];
                Object hFHOPEHSLC_RECIPE_ID = obj[i++];
                Object hFHOPEHSRECIPE_ID = obj[i++];
                Object hFHOPEHSPH_RECIPE_ID = obj[i++];
                Object hFHOPEHSRETICLE_COUNT = obj[i++];
                Object hFHOPEHSFIXTURE_COUNT = obj[i++];
                Object hFHOPEHSRPARM_COUNT = obj[i++];
                Object hFHOPEHSINIT_HOLD_FLAG = obj[i++];
                Object hFHOPEHSLAST_HLDREL_FLAG = obj[i++];
                Object hFHOPEHSHOLD_TIME = obj[i++];
                Object hFHOPEHSHOLD_USER_ID = obj[i++];
                Object hFHOPEHSHOLD_TYPE = obj[i++];
                Object hFHOPEHSHOLD_REASON_CODE = obj[i++];
                Object hFHOPEHSHOLD_REASON_DESC = obj[i++];
                Object hFHOPEHSREASON_CODE = obj[i++];
                Object hFHOPEHSREASON_DESCRIPTION = obj[i++];
                Object hFHOPEHSBANK_ID = obj[i++];
                Object hFHOPEHSPREV_BANK_ID = obj[i++];
                Object hFHOPEHSPREV_MAINPD_ID = obj[i++];
                Object hFHOPEHSPREV_OPE_NO = obj[i++];
                Object hFHOPEHSPREV_PD_ID = obj[i++];
                Object hFHOPEHSPREV_PD_TYPE = obj[i++];
                Object hFHOPEHSPREV_PD_NAME = obj[i++];
                Long hFHOPEHSPREV_PASS_COUNT = CimLongUtils.longValue(obj[i++]);
                Object hFHOPEHSPREV_STAGE_ID = obj[i++];
                Object hFHOPEHSPREV_STAGEGRP_ID = obj[i++];
                Object hFHOPEHSPREV_PHOTO_LAYER = obj[i++];
                Object hFHOPEHSFLOWBATCH_ID = obj[i++];
                Object hFHOPEHSCTRL_JOB = obj[i++];
                Object hFHOPEHSREWORK_COUNT = obj[i++];
                Object hFHOPEHSORG_WAFER_QTY = obj[i++];
                Object hFHOPEHSCUR_WAFER_QTY = obj[i++];
                Object hFHOPEHSPROD_WAFER_QTY = obj[i++];
                Object hFHOPEHSCNTL_WAFER_QTY = obj[i++];
                Object hFHOPEHSCLAIM_PROD_QTY = obj[i++];
                Object hFHOPEHSCLAIM_CNTL_QTY = obj[i++];
                Object hFHOPEHSTOTAL_GOOD_UNIT = obj[i++];
                Object hFHOPEHSTOTAL_FAIL_UNIT = obj[i++];
                Object hFHOPEHSLOT_OWNER_ID = obj[i++];
                Object hFHOPEHSPLAN_END_TIME = obj[i++];
                Object hFHOPEHSWFRHS_TIME = obj[i++];
                Object hFHOPEHSCRITERIA_FLAG = obj[i++];
                Object hFHOPEHSCLAIM_MEMO = obj[i++];
                Object hFHOPEHSRPARM_CHANGE_TYPE = obj[i++];
                Object hFHOPEHSSTORE_TIME = obj[i++];
                Object hFHOPEHSRELATED_LOT_ID = obj[i++];
                Object hFHOPEHSBOND_GRP_ID = obj[i++];
                Object hDPT_NAME_PLATE = obj[i++];
                Object hMON_GRP_ID = obj[i++];

                /*PPT_DR_CHECK_FETCH_ERROR(strOperationHistoryFillInTxPLQ008DROut,operationHistory_FillInTxPLQ008DR__160,
                        MSG_NOT_FOUND_REQD_OPEHIS,RC_NOT_FOUND_REQD_OPEHIS,
                        SQL FETCH (OHLOTOPE),count1);*/

                if( count1 >= t_len1 ) {
                    t_len1 = t_len1 + StandardProperties.OM_OPEHIST_EXTLEN_FOR_INQ_WITH_PIN_POINT.getLongValue();
                }

                Infos.OperationHisInfo operationHisInfo=new Infos.OperationHisInfo();

                if ( ( CimStringUtils.equals((String) hFHOPEHSPREV_MAINPD_ID,routeID.getValue()) ) &&
                        ( CimStringUtils.equals((String) hFHOPEHSPREV_OPE_NO,operationNumber) ) &&
                        (Objects.equals(hFHOPEHSPREV_PASS_COUNT, lvhFHOPEHSOPE_PASS_COUNT)) &&
                        ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE) ) ) ) {

                    if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOTHOLDRELEASE) )      ||
                            ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_OPERATIONCOMPLETE) ) ||
                            ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_FORCECOMP) ) ||
                            ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_OPECOMPPARTIAL) ) ||
                            ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_GATEPASS) ) ) {

                        operationHisInfo.setRouteID           (  (String) (hFHOPEHSPREV_MAINPD_ID));
                        operationHisInfo.setOperationNumber   (  (String) (hFHOPEHSPREV_OPE_NO));
                        operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSPREV_PASS_COUNT));
                        operationHisInfo.setOperationID       (  (String) (hFHOPEHSPREV_PD_ID));
                        operationHisInfo.setPdType            (  (String) (hFHOPEHSPREV_PD_TYPE));
                        operationHisInfo.setOperationName     (  (String) (hFHOPEHSPREV_PD_NAME));
                        operationHisInfo.setStageID           (  (String) (hFHOPEHSPREV_STAGE_ID));
                        operationHisInfo.setStageGroupID      (  (String) (hFHOPEHSPREV_STAGEGRP_ID));
                        operationHisInfo.setMaskLevel         (  (String) (hFHOPEHSPREV_PHOTO_LAYER));
                    } else {
                        continue;
                    }
                } else {
                    if ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION) ) ||
                            ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE) ) ) {

                        if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_SCHEDULECHANGE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLIT) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL) ) ) {

                            continue;
                        } else {

                            operationHisInfo.setRouteID           ((String) (hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   ((String) (hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       ((String) (hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            ((String) (hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     ((String) (hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           ((String) (hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      ((String) (hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         ((String) (hFHOPEHSPHOTO_LAYER));
                        }
                    } else if ( ( CimStringUtils.equals(CimObjectUtils.toString(hFHOPEHSMOVE_TYPE),BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION) ) ||
                            ( CimStringUtils.equals(CimObjectUtils.toString(hFHOPEHSMOVE_TYPE),BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE) ) ) {

                        if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_SCHEDULECHANGE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLIT) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL) ) ) {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPHOTO_LAYER));
                        } else {

                            continue;
                        }
                    } else {

                        if ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE) ) {

                            continue;
                        } else {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPHOTO_LAYER));
                        }
                    }
                }

                operationHisInfo.setReportTimeStamp           (CimObjectUtils.toString(hFHOPEHSCLAIM_TIME));
                operationHisInfo.setOperationCategory         ((String) (hFHOPEHSOPE_CATEGORY));
                operationHisInfo.setLocationID                ((String) (hFHOPEHSLOCATION_ID));
                operationHisInfo.setWorkArea                  ((String) (hFHOPEHSAREA_ID));
                operationHisInfo.setEquipmentID               ((String) (hFHOPEHSEQP_ID));
                operationHisInfo.setEquipmentName             ((String) (hFHOPEHSEQP_NAME));
                operationHisInfo.setOperationMode             ((String) (hFHOPEHSOPE_MODE));
                operationHisInfo.setLogicalRecipeID           ((String) (hFHOPEHSLC_RECIPE_ID));
                operationHisInfo.setMachineRecipeID           ((String) (hFHOPEHSRECIPE_ID));
                operationHisInfo.setPhysicalRecipeID          ((String) (hFHOPEHSPH_RECIPE_ID));
                operationHisInfo.setFlowBatchID               ((String) (hFHOPEHSFLOWBATCH_ID));
                operationHisInfo.setControlJobID              ((String) (hFHOPEHSCTRL_JOB));
                operationHisInfo.setReworkCount               (CimLongUtils.longValue(hFHOPEHSREWORK_COUNT));

                String buffer1;
                buffer1 = CimObjectUtils.toString(hFHOPEHSINIT_HOLD_FLAG);
                operationHisInfo.setInitialHoldFlag(buffer1);

                buffer1= CimObjectUtils.toString(hFHOPEHSLAST_HLDREL_FLAG);
                operationHisInfo.setLastHoldReleaseFlag       (CimObjectUtils.toString(buffer1));
                operationHisInfo.setHoldType                  (CimObjectUtils.toString(hFHOPEHSHOLD_TYPE));
                operationHisInfo.setHoldTimeStamp             (CimObjectUtils.toString(hFHOPEHSHOLD_TIME));
                operationHisInfo.setHoldUserID                (CimObjectUtils.toString(hFHOPEHSHOLD_USER_ID));
                operationHisInfo.setHoldReasonCodeID          (CimObjectUtils.toString(hFHOPEHSHOLD_REASON_CODE));
                operationHisInfo.setHoldReasonCodeDescription (CimObjectUtils.toString(hFHOPEHSHOLD_REASON_DESC));
                operationHisInfo.setReasonCodeID              (CimObjectUtils.toString(hFHOPEHSREASON_CODE));
                operationHisInfo.setReasonCodeDescription     (CimObjectUtils.toString(hFHOPEHSREASON_DESCRIPTION));
                operationHisInfo.setBankID                    (CimObjectUtils.toString(hFHOPEHSBANK_ID));
                operationHisInfo.setPreviousBankID            (CimObjectUtils.toString(hFHOPEHSPREV_BANK_ID));
                operationHisInfo.setCassetteID                (CimObjectUtils.toString(hFHOPEHSCAST_ID));
                operationHisInfo.setCassetteCategory          (CimObjectUtils.toString(hFHOPEHSCAST_CATEGORY));
                operationHisInfo.setProductType               (CimObjectUtils.toString(hFHOPEHSPROD_TYPE));
                operationHisInfo.setTestType                  (CimObjectUtils.toString(hFHOPEHSTEST_TYPE));
                operationHisInfo.setExternalPriority          (CimObjectUtils.toString(hFHOPEHSEXT_PRIORITY));
                operationHisInfo.setPriorityClass             (CimObjectUtils.toString(hFHOPEHSPRIORITY_CLASS));
                operationHisInfo.setProductID                 (CimObjectUtils.toString(hFHOPEHSPRODSPEC_ID));
                operationHisInfo.setProductGroupID            (CimObjectUtils.toString(hFHOPEHSPRODGRP_ID));
                operationHisInfo.setTechnologyID              (CimObjectUtils.toString(hFHOPEHSTECH_ID));
                operationHisInfo.setCustomerCode              (CimObjectUtils.toString(hFHOPEHSCUSTOMER_ID));
                operationHisInfo.setCustomerProductID         (CimObjectUtils.toString(hFHOPEHSCUSTPROD_ID));
                operationHisInfo.setOrderNumber               (CimObjectUtils.toString(hFHOPEHSORDER_NO));
                operationHisInfo.setLotOwnerID                (CimObjectUtils.toString(hFHOPEHSLOT_OWNER_ID));
                operationHisInfo.setDueTimeStamp              (CimObjectUtils.toString(hFHOPEHSPLAN_END_TIME));
                operationHisInfo.setWaferHistoryTimeStamp     (CimObjectUtils.toString(hFHOPEHSWFRHS_TIME));
                operationHisInfo.setOriginalWaferQuantity     (CimLongUtils.longValue(hFHOPEHSORG_WAFER_QTY));
                operationHisInfo.setCurrentWaferQuantity      (CimLongUtils.longValue(hFHOPEHSCUR_WAFER_QTY));
                operationHisInfo.setProductWaferQuantity      (CimLongUtils.longValue(hFHOPEHSPROD_WAFER_QTY));
                operationHisInfo.setControlWaferQuantity      (CimLongUtils.longValue(hFHOPEHSCNTL_WAFER_QTY));
                operationHisInfo.setClaimedProductWaferQuantity (CimLongUtils.longValue(hFHOPEHSCLAIM_PROD_QTY));
                operationHisInfo.setClaimedControlWaferQuantity (CimLongUtils.longValue(hFHOPEHSCLAIM_CNTL_QTY));
                operationHisInfo.setTotalGoodUnit             (CimLongUtils.longValue(hFHOPEHSTOTAL_GOOD_UNIT));
                operationHisInfo.setTotalFailUnit             (CimLongUtils.longValue(hFHOPEHSTOTAL_FAIL_UNIT));
                operationHisInfo.setTestCriteriaFlag          (CimBooleanUtils.convert(hFHOPEHSCRITERIA_FLAG));
                operationHisInfo.setUserID                    ((String) (hFHOPEHSCLAIM_USER_ID));
                operationHisInfo.setClaimMemo                 ((String) (hFHOPEHSCLAIM_MEMO));
                operationHisInfo.setStoreTimeStamp            (CimObjectUtils.toString(hFHOPEHSSTORE_TIME));
                operationHisInfo.setRelatedLotID              ((String) (hFHOPEHSRELATED_LOT_ID));
                operationHisInfo.setBondingGroupID            ((String) (hFHOPEHSBOND_GRP_ID));
                operationHisInfo.setRecipeParameterChangeType ((String) (hFHOPEHSRPARM_CHANGE_TYPE));
                operationHisInfo.setMonitorGroupId ((String) (hMON_GRP_ID));

                String departmentNamePlate = (String) (hDPT_NAME_PLATE);
                if (CimStringUtils.isNotEmpty(departmentNamePlate)) {
                    List<String> departmentAndSection = Arrays.stream(departmentNamePlate.split("\\.", 2)).collect(Collectors.toList());
                    if (CimNumberUtils.eq(departmentAndSection.size(), 2)) {
                        operationHisInfo.setDepartment(departmentAndSection.get(0));
                        operationHisInfo.setSection(departmentAndSection.get(1));
                    }
                }

                sql="select RTCL_ID,LOT_ID\n" +
                        "                from   OHLOTOPE_RTCL\n" +
                        "                where LOT_ID             = ?            and\n" +
                        "                PROCESS_ID          = ?     and\n" +
                        "                OPE_NO             = ?     and\n" +
                        "                OPE_PASS_COUNT     = ?     and\n" +
                        "                TRX_TIME         = ?     and\n" +
                        "                OPE_CATEGORY       = ?";
                List<Object[]> v12__160 = cimJpaRepository.query(sql, hFHOPEHSLOT_ID, hFHOPEHSMAINPD_ID, hFHOPEHSOPE_NO, hFHOPEHSOPE_PASS_COUNT, hFHOPEHSCLAIM_TIME, hFHOPEHSOPE_CATEGORY);


                Long t_len2 = nReticleSeqLen;
                Long count2 = 0L;
                List<Infos.OpeHisReticleInfo> strOpeHisReticleInfo=new ArrayList<>();
                operationHisInfo.setStrOpeHisReticleInfo(strOpeHisReticleInfo);

                for (Object[] object:v12__160) {
                    String hFHOPEHS_RETICLERETICLE_ID=(String) object[0];

                    if( count2 >= t_len2 ) {
                        t_len2 = t_len2 + nReticleSeqLen;
                    }

                    Infos.OpeHisReticleInfo opeHisReticleInfo=new Infos.OpeHisReticleInfo();
                    strOpeHisReticleInfo.add(opeHisReticleInfo);
                    opeHisReticleInfo.setReticleID(hFHOPEHS_RETICLERETICLE_ID);
                    count2++;
                }

                sql="select RTCL_ID,LOT_ID\n" +
                        "                from   OHLOTOPE_FIXT\n" +
                        "                where LOT_ID             = ?             and\n" +
                        "                PROCESS_ID          = ?     and\n" +
                        "                OPE_NO             = ?     and\n" +
                        "                OPE_PASS_COUNT     = ?     and\n" +
                        "                TRX_TIME         = ?     and\n" +
                        "                OPE_CATEGORY       = ?";
                List<Object[]> v13__160 = cimJpaRepository.query(sql, hFHOPEHSLOT_ID, hFHOPEHSMAINPD_ID
                        , hFHOPEHSOPE_NO
                        , hFHOPEHSOPE_PASS_COUNT
                        , hFHOPEHSCLAIM_TIME
                        , hFHOPEHSOPE_CATEGORY);

                Long t_len3 = nFixtureSeqLen;
                Long count3 = 0L;
                List<Infos.OpeHisFixtureInfo> strOpeHisFixtureInfo=new ArrayList<>();
                operationHisInfo.setStrOpeHisFixtureInfo(strOpeHisFixtureInfo);

                for (Object[] object: v13__160) {
                    String hFHOPEHS_FIXTUREFIXTURE_ID= (String) object[0];

                    if( count3 >= t_len3 ) {
                        t_len3 = t_len3 + nFixtureSeqLen;
                    }

                    Infos.OpeHisFixtureInfo opeHisFixtureInfo=new Infos.OpeHisFixtureInfo();
                    strOpeHisFixtureInfo.add(opeHisFixtureInfo);
                    opeHisFixtureInfo.setFixtureID(hFHOPEHS_FIXTUREFIXTURE_ID);
                    count3++;
                }


                Infos.LotOpeHisRParmGetDROut  strLotOpeHisRParmGetDROut;
                // step1 - lot_opeHisRParm_GetDR
                strLotOpeHisRParmGetDROut = lotMethod.lotOpeHisRParmGetDR(strObjCommonIn,
                        (String) hFHOPEHSLOT_ID,
                        (String) hFHOPEHSMAINPD_ID,
                        (String) hFHOPEHSOPE_NO,
                        CimLongUtils.longValue(hFHOPEHSOPE_PASS_COUNT),
                        CimObjectUtils.toString(hFHOPEHSCLAIM_TIME),
                        (String) hFHOPEHSOPE_CATEGORY);

                if ( CimStringUtils.equals(BizConstant.SP_RPARM_CHANGETYPE_BYWAFER, (String) hFHOPEHSRPARM_CHANGE_TYPE) ) {
                    operationHisInfo.setStrOpeHisRecipeParmInfo (strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmInfo());
                    operationHisInfo.setStrOpeHisRecipeParmWaferInfo (strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmWaferInfo());
                } else {
                    operationHisInfo.setStrOpeHisRecipeParmInfo (strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmInfo());
                    operationHisInfo.setStrOpeHisRecipeParmWaferInfo(new ArrayList<>());
                }

                strOperationHisInfo.add(operationHisInfo);

                count1++;
                if ( count1 >= lenOpeHisMax ) {
                    break;
                }
            }

        } else {

            sql="select\n" +
                    "                    PROCESS_ID,\n" +
                    "                    OPE_NO,\n" +
                    "                    OPE_PASS_COUNT,\n" +
                    "                    TRX_TIME,\n" +
                    "                    MOVE_TYPE,\n" +
                    "                    OPE_CATEGORY,\n" +
                    "                    PREV_PROCESS_ID,\n" +
                    "                    PREV_OPE_NO,\n" +
                    "                    PREV_PASS_COUNT\n" +
                    "            from  OHLOTOPE\n" +
                    "            where\n" +
                    "            LOT_ID             = ?          and\n" +
                    "            ( ( PROCESS_ID      = ?          and\n" +
                    "            OPE_NO             = ?     and\n" +
                    "            OPE_PASS_COUNT     = ? )\n" +
                    "            or\n" +
                    "          ( PREV_PROCESS_ID     = ?     and\n" +
                    "            PREV_OPE_NO        = ?     and\n" +
                    "            PREV_PASS_COUNT    = ?     and\n" +
                    "         (  MOVE_TYPE = ? or\n" +
                    "            MOVE_TYPE = ?        ) ) )\n";

            //add fromTimeStamp and toTimeStamp for history query
            String sqlTemp = "";
            if (CimStringUtils.isNotEmpty( fromTimeStamp )) {
                sqlTemp = String.format(" AND EVENT_CREATE_TIME >= TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", fromTimeStamp);
                sql += sqlTemp ;
            }
            if (CimStringUtils.isNotEmpty( toTimeStamp )) {
                sqlTemp = String.format(" AND EVENT_CREATE_TIME <= TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", toTimeStamp);
                sql += sqlTemp ;
            }
            sql += "            order by TRX_TIME, EVENT_CREATE_TIME, STORE_TIME";

            List<Object[]> v20__160=cimJpaRepository.query(sql,lvhFHOPEHSLOT_ID ,lvhFHOPEHSMAINPD_ID ,
                    lvhFHOPEHSOPE_NO ,lvhFHOPEHSOPE_PASS_COUNT ,
                    lvhFHOPEHSMAINPD_ID ,lvhFHOPEHSOPE_NO ,
                    lvhFHOPEHSOPE_PASS_COUNT ,lvhFHOPEHSMOVE_TYPE ,
                    lvhFHOPEHSMOVE_TYPE_1);

            Boolean foundFlag = FALSE;

            Object lvhFHOPEHSCLAIM_TIME=null;
            for (Object[] obj:v20__160) {
                Object hFHOPEHSMAINPD_ID = obj[0];
                Object hFHOPEHSOPE_NO = obj[1];
                Object hFHOPEHSOPE_PASS_COUNT = obj[2];
                lvhFHOPEHSCLAIM_TIME = obj[3];
                Object hFHOPEHSMOVE_TYPE = obj[4];
                Object hFHOPEHSOPE_CATEGORY = obj[5];
                Object hFHOPEHSPREV_MAINPD_ID = obj[6];
                Object hFHOPEHSPREV_OPE_NO = obj[7];
                Long hFHOPEHSPREV_PASS_COUNT = CimLongUtils.longValue(obj[8]);

                if ( ( CimStringUtils.length(operationCategory) != 0 ) &&
                        ( !CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,operationCategory)  ) ) {

                    continue;

                }

                if ( ( CimStringUtils.equals((String) hFHOPEHSPREV_MAINPD_ID,routeID.getValue())) &&
                        ( CimStringUtils.equals((String) hFHOPEHSPREV_OPE_NO,operationNumber)) &&
                        (Objects.equals(hFHOPEHSPREV_PASS_COUNT, lvhFHOPEHSOPE_PASS_COUNT)) &&
                        ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE) ) )) {

                    if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY, BizConstant.SP_OPERATIONCATEGORY_LOTHOLDRELEASE))      ||
                            ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY, BizConstant.SP_OPERATIONCATEGORY_OPERATIONCOMPLETE)) ||
                            ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY, BizConstant.SP_OPERATIONCATEGORY_FORCECOMP)) ||
                            ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY, BizConstant.SP_OPERATIONCATEGORY_OPECOMPPARTIAL)) ||
                            ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY, BizConstant.SP_OPERATIONCATEGORY_GATEPASS)) ) {

                        foundFlag = TRUE;
                        break;
                    } else {

                        continue;
                    }
                } else {
                    if ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION)) ||
                            ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE)) ) {

                        if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_SCHEDULECHANGE)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLIT)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL)) ) {

                            continue;
                        } else {

                            foundFlag = TRUE;
                            break;
                        }
                    } else if ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION)) ||
                            ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE)) ) {

                        if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_SCHEDULECHANGE)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLIT)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL)) ) {

                            foundFlag = TRUE;
                            break;
                        } else {

                            continue;
                        }
                    } else {

                        if ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE) ) {

                            continue;
                        } else {

                            foundFlag = TRUE;
                            break;
                        }
                    }
                }
            }
            Validations.check(!foundFlag, retCodeConfig.getNotFoundReqdOpehis());

            if ( CimStringUtils.length(operationCategory) != 0 ) {

                String hFHOPEHSOPE_CATEGORY=operationCategory;

                sql="select\n" +
                        "                        LOT_ID,\n" +
                        "                        LOT_TYPE,\n" +
                        "                        CARRIER_ID,\n" +
                        "                        CARRIER_CATEGORY,\n" +
                        "                        PROCESS_ID,\n" +
                        "                        OPE_NO,\n" +
                        "                        STEP_ID,\n" +
                        "                STEP_TYPE,\n" +
                        "                OPE_PASS_COUNT,\n" +
                        "                        STEP_NAME,\n" +
                        "                        HOLD_STATE,\n" +
                        "                        TRX_TIME,\n" +
                        "                        '',\n" +
                        "                        TRX_USER_ID,\n" +
                        "                        MOVE_TYPE,\n" +
                        "                        OPE_CATEGORY,\n" +
                        "                        PROD_TYPE,\n" +
                        "                        TEST_TYPE,\n" +
                        "                        MFG_LAYER,\n" +
                        "                        '',\n" +
                        "                        LOT_PRIORITY,\n" +
                        "                        PREV_PROD_ID,\n" +
                        "                        PROD_ID,\n" +
                        "                        PRODFMLY_ID,\n" +
                        "                        TECH_ID,\n" +
                        "                        CUSTOMER_ID,\n" +
                        "                        CUSTPROD_ID,\n" +
                        "                        MFG_ORDER_NO,\n" +
                        "                        STAGE_ID,\n" +
                        "                        STAGE_GRP_ID,\n" +
                        "                        PHOTO_LAYER,\n" +
                        "                        LOCATION_ID,\n" +
                        "                        BAY_ID,\n" +
                        "                        EQP_ID,\n" +
                        "                        EQP_NAME,\n" +
                        "                        OPE_MODE,\n" +
                        "                        LRCP_ID,\n" +
                        "                        MRCP_ID,\n" +
                        "                        PRCP_ID,\n" +
                        "                        RETICLE_COUNT,\n" +
                        "                        FIXTURE_COUNT,\n" +
                        "                        RPARAM_COUNT,\n" +
                        "                        INITIAL_HOLD_FLAG,\n" +
                        "                        LAST_HOLD_REL_FLAG,\n" +
                        "                        HOLD_TRX_TIME,\n" +
                        "                        '',\n" +
                        "                        HOLD_USER_ID,\n" +
                        "                        HOLD_TYPE,\n" +
                        "                        HOLD_REASON_CODE,\n" +
                        "                        HOLD_REASON_DESC,\n" +
                        "                        REASON_CODE,\n" +
                        "                        REASON_DESC,\n" +
                        "                        BANK_ID,\n" +
                        "                        PREV_BANK_ID,\n" +
                        "                        PREV_PROCESS_ID,\n" +
                        "                        PREV_OPE_NO,\n" +
                        "                        PREV_STEP_ID,\n" +
                        "                        PREV_STEP_TYPE,\n" +
                        "                PREV_STEP_NAME,\n" +
                        "                        PREV_PASS_COUNT,\n" +
                        "                        PREV_STAGE_ID,\n" +
                        "                        PREV_STAGE_GRP_ID,\n" +
                        "                        PREV_PHOTO_LAYER,\n" +
                        "                        FLOWB_ID,\n" +
                        "                        CJ_ID,\n" +
                        "                        REWORK_COUNT,\n" +
                        "                        ORIG_WAFER_QTY,\n" +
                        "                        CUR_WAFER_QTY,\n" +
                        "                        PROD_WAFER_QTY,\n" +
                        "                        NPW_WAFER_QTY,\n" +
                        "                        CLAIM_PROD_QTY,\n" +
                        "                        TRX_NPW_QTY,\n" +
                        "                        TOTAL_GOOD_UNIT,\n" +
                        "                        TOTAL_FAIL_UNIT,\n" +
                        "                        LOT_OWNER_ID,\n" +
                        "                        PLAN_END_TIME,\n" +
                        "                        WAFER_HIS_TIME,\n" +
                        "                        CRITERIA_FLAG,\n" +
                        "                        TRX_MEMO,\n" +
                        "                        RPARAM_CHG_TYPE,\n" +
                        "                STORE_TIME,\n" +
                        "                        RELATED_LOT_ID,   \n" +
                        "            BOND_GRP_ID,             \n" +
                        "            DPT_NAME_PLATE,            \n" +
                        "            MON_GRP_ID             \n" +
                        "                from  OHLOTOPE\n" +
                        "                where LOT_ID       = ?         and\n" +
                        "                OPE_CATEGORY       = ?         and\n" +
                        "                TRX_TIME        >= ?\n";

                //add fromTimeStamp and toTimeStamp for history query
                sqlTemp = "";
                if (CimStringUtils.isNotEmpty( fromTimeStamp )) {
                    sqlTemp = String.format(" AND EVENT_CREATE_TIME >= TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", fromTimeStamp);
                    sql += sqlTemp ;
                }
                if (CimStringUtils.isNotEmpty( toTimeStamp )) {
                    sqlTemp = String.format(" AND EVENT_CREATE_TIME <= TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", toTimeStamp);
                    sql += sqlTemp ;
                }

                sql += "                order by TRX_TIME, EVENT_CREATE_TIME, STORE_TIME";

                List<Object[]> v21__160=cimJpaRepository.query(sql,lvhFHOPEHSLOT_ID
                        , hFHOPEHSOPE_CATEGORY
                        , lvhFHOPEHSCLAIM_TIME);

                Long t_len1 = StandardProperties.OM_OPEHIST_EXTLEN_FOR_INQ.getLongValue();
                Long count1 = 0L;

                for (Object[] obj: v21__160) {
                    Object hFHOPEHSLOT_ID=obj[0];
                    Object hFHOPEHSLOT_TYPE=obj[1];
                    Object hFHOPEHSCAST_ID=obj[2];
                    Object hFHOPEHSCAST_CATEGORY=obj[3];
                    Object hFHOPEHSMAINPD_ID=obj[4];
                    Object hFHOPEHSOPE_NO=obj[5];
                    Object hFHOPEHSPD_ID=obj[6];
                    Object hFHOPEHSPD_TYPE=obj[7];
                    Object hFHOPEHSOPE_PASS_COUNT=obj[8];
                    Object hFHOPEHSPD_NAME=obj[9];
                    Object hFHOPEHSHOLD_STATE=obj[10];
                    Object hFHOPEHSCLAIM_TIME=obj[11];
                    Object hFHOPEHSCLAIM_SHOP_DATE=obj[12];
                    Object hFHOPEHSCLAIM_USER_ID=obj[13];
                    Object hFHOPEHSMOVE_TYPE=obj[14];
                    hFHOPEHSOPE_CATEGORY= (String) obj[15];
                    Object hFHOPEHSPROD_TYPE=obj[16];
                    Object hFHOPEHSTEST_TYPE=obj[17];
                    Object hFHOPEHSMFG_LAYER=obj[18];
                    Object hFHOPEHSEXT_PRIORITY=obj[19];
                    Object hFHOPEHSPRIORITY_CLASS=obj[20];
                    Object hFHOPEHSPREV_PRODSPEC_ID=obj[21];
                    Object hFHOPEHSPRODSPEC_ID=obj[22];
                    Object hFHOPEHSPRODGRP_ID=obj[23];
                    Object hFHOPEHSTECH_ID=obj[24];
                    Object hFHOPEHSCUSTOMER_ID=obj[25];
                    Object hFHOPEHSCUSTPROD_ID=obj[26];
                    Object hFHOPEHSORDER_NO=obj[27];
                    Object hFHOPEHSSTAGE_ID=obj[28];
                    Object hFHOPEHSSTAGEGRP_ID=obj[29];
                    Object hFHOPEHSPHOTO_LAYER=obj[30];
                    Object hFHOPEHSLOCATION_ID=obj[31];
                    Object hFHOPEHSAREA_ID=obj[32];
                    Object hFHOPEHSEQP_ID=obj[33];
                    Object hFHOPEHSEQP_NAME=obj[34];
                    Object hFHOPEHSOPE_MODE=obj[35];
                    Object hFHOPEHSLC_RECIPE_ID=obj[36];
                    Object hFHOPEHSRECIPE_ID=obj[37];
                    Object hFHOPEHSPH_RECIPE_ID=obj[38];
                    Object hFHOPEHSRETICLE_COUNT=obj[39];
                    Object hFHOPEHSFIXTURE_COUNT=obj[40];
                    Object hFHOPEHSRPARM_COUNT=obj[41];
                    Object hFHOPEHSINIT_HOLD_FLAG=obj[42];
                    Object hFHOPEHSLAST_HLDREL_FLAG=obj[43];
                    Object hFHOPEHSHOLD_TIME=obj[44];
                    Object hFHOPEHSHOLD_SHOP_DATE=obj[45];
                    Object hFHOPEHSHOLD_USER_ID=obj[46];
                    Object hFHOPEHSHOLD_TYPE=obj[47];
                    Object hFHOPEHSHOLD_REASON_CODE=obj[48];
                    Object hFHOPEHSHOLD_REASON_DESC=obj[49];
                    Object hFHOPEHSREASON_CODE=obj[50];
                    Object hFHOPEHSREASON_DESCRIPTION=obj[51];
                    Object hFHOPEHSBANK_ID=obj[52];
                    Object hFHOPEHSPREV_BANK_ID=obj[53];
                    Object hFHOPEHSPREV_MAINPD_ID=obj[54];
                    Object hFHOPEHSPREV_OPE_NO=obj[55];
                    Object hFHOPEHSPREV_PD_ID=obj[56];
                    Object hFHOPEHSPREV_PD_TYPE=obj[57];
                    Object hFHOPEHSPREV_PD_NAME=obj[58];
                    Long hFHOPEHSPREV_PASS_COUNT= CimLongUtils.longValue(obj[59]);
                    Object hFHOPEHSPREV_STAGE_ID=obj[60];
                    Object hFHOPEHSPREV_STAGEGRP_ID=obj[61];
                    Object hFHOPEHSPREV_PHOTO_LAYER=obj[62];
                    Object hFHOPEHSFLOWBATCH_ID=obj[63];
                    Object hFHOPEHSCTRL_JOB=obj[64];
                    Object hFHOPEHSREWORK_COUNT=obj[65];
                    Object hFHOPEHSORG_WAFER_QTY=obj[66];
                    Object hFHOPEHSCUR_WAFER_QTY=obj[67];
                    Object hFHOPEHSPROD_WAFER_QTY=obj[68];
                    Object hFHOPEHSCNTL_WAFER_QTY=obj[69];
                    Object hFHOPEHSCLAIM_PROD_QTY=obj[70];
                    Object hFHOPEHSCLAIM_CNTL_QTY=obj[71];
                    Object hFHOPEHSTOTAL_GOOD_UNIT=obj[72];
                    Object hFHOPEHSTOTAL_FAIL_UNIT=obj[73];
                    Object hFHOPEHSLOT_OWNER_ID=obj[74];
                    Object hFHOPEHSPLAN_END_TIME=obj[75];
                    Object hFHOPEHSWFRHS_TIME=obj[76];
                    Object hFHOPEHSCRITERIA_FLAG=obj[77];
                    Object hFHOPEHSCLAIM_MEMO=obj[78];
                    Object hFHOPEHSRPARM_CHANGE_TYPE=obj[79];
                    Object hFHOPEHSSTORE_TIME=obj[80];
                    Object hFHOPEHSRELATED_LOT_ID=obj[81];
                    Object hFHOPEHSBOND_GRP_ID=obj[82];
                    Object hDPT_NAME_PLATE=obj[83];
                    Object hMON_GRP_ID=obj[84];


                    if( count1 >= t_len1 ) {
                        t_len1 = t_len1 + StandardProperties.OM_OPEHIST_EXTLEN_FOR_INQ.getLongValue();
                    }

                    Infos.OperationHisInfo operationHisInfo=new Infos.OperationHisInfo();

                    if ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION)) ||
                            ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE)) ) {

                        if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_SCHEDULECHANGE)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLIT)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL)) ) {

                            continue; // ignore this record and fetch next record
                        } else {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPHOTO_LAYER));
                        }
                    } else if ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION) ) ||
                            ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE) ) ) {

                        if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOTHOLDRELEASE) )      ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_OPERATIONCOMPLETE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_FORCECOMP) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_OPECOMPPARTIAL) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_GATEPASS) )  ) {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSPREV_MAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSPREV_OPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSPREV_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPREV_PD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPREV_PD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPREV_PD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSPREV_STAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSPREV_STAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPREV_PHOTO_LAYER));
                        } else if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_SCHEDULECHANGE)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLIT)) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL)) ) {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPHOTO_LAYER));
                        } else {

                            continue;
                        }
                    } else {

                        if ( CimStringUtils.equals(hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE) ) {

                            continue;
                        } else {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPHOTO_LAYER));
                        }
                    }


                    operationHisInfo.setReportTimeStamp           (CimObjectUtils.toString(hFHOPEHSCLAIM_TIME));
                    operationHisInfo.setOperationCategory         ((String) (hFHOPEHSOPE_CATEGORY));
                    operationHisInfo.setLocationID                ((String) (hFHOPEHSLOCATION_ID));
                    operationHisInfo.setWorkArea                  ((String) (hFHOPEHSAREA_ID));
                    operationHisInfo.setEquipmentID               ((String) (hFHOPEHSEQP_ID));
                    operationHisInfo.setEquipmentName             ((String) (hFHOPEHSEQP_NAME));
                    operationHisInfo.setOperationMode             ((String) (hFHOPEHSOPE_MODE));
                    operationHisInfo.setLogicalRecipeID           ((String) (hFHOPEHSLC_RECIPE_ID));
                    operationHisInfo.setMachineRecipeID           ((String) (hFHOPEHSRECIPE_ID));
                    operationHisInfo.setPhysicalRecipeID          ((String) (hFHOPEHSPH_RECIPE_ID));
                    operationHisInfo.setFlowBatchID               ((String) (hFHOPEHSFLOWBATCH_ID));
                    operationHisInfo.setControlJobID            ((String) (hFHOPEHSCTRL_JOB));
                    operationHisInfo.setReworkCount               (CimLongUtils.longValue(hFHOPEHSREWORK_COUNT));

                    String buffer2;
                    buffer2 = CimObjectUtils.toString(hFHOPEHSINIT_HOLD_FLAG);
                    operationHisInfo.setInitialHoldFlag           ((buffer2));

                    buffer2 = CimObjectUtils.toString(hFHOPEHSLAST_HLDREL_FLAG);
                    operationHisInfo.setLastHoldReleaseFlag       ((buffer2));

                    operationHisInfo.setHoldType                  (CimObjectUtils.toString(hFHOPEHSHOLD_TYPE));
                    operationHisInfo.setHoldTimeStamp             (CimObjectUtils.toString(hFHOPEHSHOLD_TIME));
                    operationHisInfo.setHoldUserID                (CimObjectUtils.toString(hFHOPEHSHOLD_USER_ID));
                    operationHisInfo.setHoldReasonCodeID          (CimObjectUtils.toString(hFHOPEHSHOLD_REASON_CODE));
                    operationHisInfo.setHoldReasonCodeDescription (CimObjectUtils.toString(hFHOPEHSHOLD_REASON_DESC));
                    operationHisInfo.setReasonCodeID              (CimObjectUtils.toString(hFHOPEHSREASON_CODE));
                    operationHisInfo.setReasonCodeDescription     (CimObjectUtils.toString(hFHOPEHSREASON_DESCRIPTION));
                    operationHisInfo.setBankID                    (CimObjectUtils.toString(hFHOPEHSBANK_ID));
                    operationHisInfo.setPreviousBankID            (CimObjectUtils.toString(hFHOPEHSPREV_BANK_ID));

                    operationHisInfo.setCassetteID                (CimObjectUtils.toString(hFHOPEHSCAST_ID));
                    operationHisInfo.setCassetteCategory          (CimObjectUtils.toString(hFHOPEHSCAST_CATEGORY));
                    operationHisInfo.setProductType               (CimObjectUtils.toString(hFHOPEHSPROD_TYPE));
                    operationHisInfo.setTestType                  (CimObjectUtils.toString(hFHOPEHSTEST_TYPE));
                    operationHisInfo.setExternalPriority          (CimObjectUtils.toString(hFHOPEHSEXT_PRIORITY));
                    operationHisInfo.setPriorityClass             (CimObjectUtils.toString(hFHOPEHSPRIORITY_CLASS));
                    operationHisInfo.setProductID                 (CimObjectUtils.toString(hFHOPEHSPRODSPEC_ID));
                    operationHisInfo.setProductGroupID            (CimObjectUtils.toString(hFHOPEHSPRODGRP_ID));
                    operationHisInfo.setTechnologyID              (CimObjectUtils.toString(hFHOPEHSTECH_ID));
                    operationHisInfo.setCustomerCode              (CimObjectUtils.toString(hFHOPEHSCUSTOMER_ID));
                    operationHisInfo.setCustomerProductID         (CimObjectUtils.toString(hFHOPEHSCUSTPROD_ID));
                    operationHisInfo.setOrderNumber               (CimObjectUtils.toString(hFHOPEHSORDER_NO));
                    operationHisInfo.setLotOwnerID                (CimObjectUtils.toString(hFHOPEHSLOT_OWNER_ID));
                    operationHisInfo.setDueTimeStamp              (CimObjectUtils.toString(hFHOPEHSPLAN_END_TIME));
                    operationHisInfo.setWaferHistoryTimeStamp     (CimObjectUtils.toString(hFHOPEHSWFRHS_TIME));
                    operationHisInfo.setOriginalWaferQuantity     (CimLongUtils.longValue(hFHOPEHSORG_WAFER_QTY));
                    operationHisInfo.setCurrentWaferQuantity      (CimLongUtils.longValue(hFHOPEHSCUR_WAFER_QTY));
                    operationHisInfo.setProductWaferQuantity      (CimLongUtils.longValue(hFHOPEHSPROD_WAFER_QTY));
                    operationHisInfo.setControlWaferQuantity      (CimLongUtils.longValue(hFHOPEHSCNTL_WAFER_QTY));
                    operationHisInfo.setClaimedProductWaferQuantity (CimLongUtils.longValue(hFHOPEHSCLAIM_PROD_QTY));
                    operationHisInfo.setClaimedControlWaferQuantity (CimLongUtils.longValue(hFHOPEHSCLAIM_CNTL_QTY));
                    operationHisInfo.setTotalGoodUnit             (CimLongUtils.longValue(hFHOPEHSTOTAL_GOOD_UNIT));
                    operationHisInfo.setTotalFailUnit             (CimLongUtils.longValue(hFHOPEHSTOTAL_FAIL_UNIT));

                    operationHisInfo.setTestCriteriaFlag          (CimBooleanUtils.convert(hFHOPEHSCRITERIA_FLAG));
                    operationHisInfo.setUserID                    (CimObjectUtils.toString(hFHOPEHSCLAIM_USER_ID));
                    operationHisInfo.setClaimMemo                 (CimObjectUtils.toString(hFHOPEHSCLAIM_MEMO));
                    operationHisInfo.setStoreTimeStamp            (CimObjectUtils.toString(hFHOPEHSSTORE_TIME));
                    operationHisInfo.setRelatedLotID              (CimObjectUtils.toString(hFHOPEHSRELATED_LOT_ID));
                    operationHisInfo.setBondingGroupID            (CimObjectUtils.toString(hFHOPEHSBOND_GRP_ID));
                    operationHisInfo.setRecipeParameterChangeType (CimObjectUtils.toString(hFHOPEHSRPARM_CHANGE_TYPE));
                    operationHisInfo.setMonitorGroupId ((String) (hMON_GRP_ID));

                    String departmentNamePlate = (String) (hDPT_NAME_PLATE);
                    if (CimStringUtils.isNotEmpty(departmentNamePlate)) {
                        List<String> departmentAndSection = Arrays.stream(departmentNamePlate.split("\\.", 2)).collect(Collectors.toList());
                        if (CimNumberUtils.eq(departmentAndSection.size(), 2)) {
                            operationHisInfo.setDepartment(departmentAndSection.get(0));
                            operationHisInfo.setSection(departmentAndSection.get(1));
                        }
                    }


                    sql="select RTCL_ID,LOT_ID\n" +
                            "                    from   OHLOTOPE_RTCL\n" +
                            "                    where LOT_ID       = ?     and\n" +
                            "                    PROCESS_ID          = ?     and\n" +
                            "                    OPE_NO             = ?     and\n" +
                            "                    OPE_PASS_COUNT     = ?     and\n" +
                            "                    TRX_TIME         = ?     and\n" +
                            "                    OPE_CATEGORY       = ?";

                    List<Object[]> v22__160=cimJpaRepository.query(sql,hFHOPEHSLOT_ID
                            ,hFHOPEHSMAINPD_ID
                            ,hFHOPEHSOPE_NO
                            ,hFHOPEHSOPE_PASS_COUNT
                            ,hFHOPEHSCLAIM_TIME
                            ,hFHOPEHSOPE_CATEGORY );


                    Long t_len2 = nReticleSeqLen;
                    Long count2 = 0L;
                    List<Infos.OpeHisReticleInfo> strOpeHisReticleInfo=new ArrayList<>();
                    operationHisInfo.setStrOpeHisReticleInfo(strOpeHisReticleInfo);

                    for (Object[] object: v22__160) {
                        Object hFHOPEHS_RETICLERETICLE_ID = object[0];

                        if( count2 >= t_len2 ) {
                            t_len2 = t_len2 + nReticleSeqLen;
                        }

                        Infos.OpeHisReticleInfo opeHisReticleInfo=new Infos.OpeHisReticleInfo();
                        strOpeHisReticleInfo.add(opeHisReticleInfo);
                        opeHisReticleInfo.setReticleID ((String) (hFHOPEHS_RETICLERETICLE_ID));
                        count2++;
                    }


                    sql="select RTCL_ID,LOT_ID\n" +
                            "                    from   OHLOTOPE_FIXT\n" +
                            "                    where LOT_ID       = ?     and\n" +
                            "                    PROCESS_ID          = ?     and\n" +
                            "                    OPE_NO             = ?     and\n" +
                            "                    OPE_PASS_COUNT     = ?     and\n" +
                            "                    TRX_TIME         = ?     and\n" +
                            "                    OPE_CATEGORY       = ?";

                    List<Object[]> v23__160=cimJpaRepository.query(sql,hFHOPEHSLOT_ID
                            ,hFHOPEHSMAINPD_ID
                            ,hFHOPEHSOPE_NO
                            ,hFHOPEHSOPE_PASS_COUNT
                            ,hFHOPEHSCLAIM_TIME
                            ,hFHOPEHSOPE_CATEGORY );


                    Long t_len3 = nFixtureSeqLen;
                    Long count3 = 0L;
                    List<Infos.OpeHisFixtureInfo> strOpeHisFixtureInfo=new ArrayList<>();
                    operationHisInfo.setStrOpeHisFixtureInfo(strOpeHisFixtureInfo);

                    for (Object[] object:v23__160) {
                        Object hFHOPEHS_FIXTUREFIXTURE_ID=object[0];

                        if( count3 >= t_len3 ) {
                            t_len3 = t_len3 + nFixtureSeqLen;
                        }

                        Infos.OpeHisFixtureInfo opeHisFixtureInfo=new Infos.OpeHisFixtureInfo();
                        strOpeHisFixtureInfo.add(opeHisFixtureInfo);
                        opeHisFixtureInfo.setFixtureID((String) hFHOPEHS_FIXTUREFIXTURE_ID);
                        count3++;
                    }


                    Infos.LotOpeHisRParmGetDROut  strLotOpeHisRParmGetDROut;
                    // step2 - lot_opeHisRParm_GetDR
                    strLotOpeHisRParmGetDROut = lotMethod.lotOpeHisRParmGetDR(strObjCommonIn,
                            (String) hFHOPEHSLOT_ID,
                            (String) hFHOPEHSMAINPD_ID,
                            (String) hFHOPEHSOPE_NO,
                            CimLongUtils.longValue(hFHOPEHSOPE_PASS_COUNT),
                            CimObjectUtils.toString(hFHOPEHSCLAIM_TIME),
                            (String) hFHOPEHSOPE_CATEGORY );

                    if ( CimStringUtils.equals(BizConstant.SP_RPARM_CHANGETYPE_BYWAFER, (String) hFHOPEHSRPARM_CHANGE_TYPE) ) {
                        operationHisInfo.setStrOpeHisRecipeParmInfo (strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmInfo());
                        operationHisInfo.setStrOpeHisRecipeParmWaferInfo (strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmWaferInfo());
                    } else {
                        operationHisInfo.setStrOpeHisRecipeParmInfo(strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmInfo());
                        operationHisInfo.setStrOpeHisRecipeParmWaferInfo(new ArrayList<>());
                    }

                    strOperationHisInfo.add(operationHisInfo);
                    count1++;

                    if ( count1 >= lenOpeHisMax ) {
                        break;
                    }
                }
            } else {

                sql="select\n" +
                        "                        LOT_ID,\n" +
                        "                        LOT_TYPE,\n" +
                        "                        CARRIER_ID,\n" +
                        "                        CARRIER_CATEGORY,\n" +
                        "                        PROCESS_ID,\n" +
                        "                        OPE_NO,\n" +
                        "                        STEP_ID,\n" +
                        "                STEP_TYPE,         \n" +
                        "                OPE_PASS_COUNT,\n" +
                        "                        STEP_NAME,\n" +
                        "                        HOLD_STATE,\n" +
                        "                        TRX_TIME,\n" +
                        "                        TRX_USER_ID,\n" +
                        "                        MOVE_TYPE,\n" +
                        "                        OPE_CATEGORY,\n" +
                        "                        PROD_TYPE,\n" +
                        "                        TEST_TYPE,\n" +
                        "                        MFG_LAYER,\n" +
                        "                        LOT_PRIORITY,\n" +
                        "                        PREV_PROD_ID,\n" +
                        "                        PROD_ID,\n" +
                        "                        PRODFMLY_ID,\n" +
                        "                        TECH_ID,\n" +
                        "                        CUSTOMER_ID,\n" +
                        "                        CUSTPROD_ID,\n" +
                        "                        MFG_ORDER_NO,\n" +
                        "                        STAGE_ID,\n" +
                        "                        STAGE_GRP_ID,\n" +
                        "                        PHOTO_LAYER,\n" +
                        "                        LOCATION_ID,\n" +
                        "                        BAY_ID,\n" +
                        "                        EQP_ID,\n" +
                        "                        EQP_NAME,\n" +
                        "                        OPE_MODE,\n" +
                        "                        LRCP_ID,\n" +
                        "                        MRCP_ID,\n" +
                        "                        PRCP_ID,\n" +
                        "                        RETICLE_COUNT,\n" +
                        "                        FIXTURE_COUNT,\n" +
                        "                        RPARAM_COUNT,\n" +
                        "                        INITIAL_HOLD_FLAG,\n" +
                        "                        LAST_HOLD_REL_FLAG,\n" +
                        "                        HOLD_TRX_TIME,\n" +
                        "                        HOLD_USER_ID,\n" +
                        "                        HOLD_TYPE,\n" +
                        "                        HOLD_REASON_CODE,\n" +
                        "                        HOLD_REASON_DESC,\n" +
                        "                        REASON_CODE,\n" +
                        "                        REASON_DESC,\n" +
                        "                        BANK_ID,\n" +
                        "                        PREV_BANK_ID,\n" +
                        "                        PREV_PROCESS_ID,\n" +
                        "                        PREV_OPE_NO,\n" +
                        "                        PREV_STEP_ID,\n" +
                        "                        PREV_STEP_TYPE,          \n" +
                        "                PREV_STEP_NAME,\n" +
                        "                        PREV_PASS_COUNT,\n" +
                        "                        PREV_STAGE_ID,\n" +
                        "                        PREV_STAGE_GRP_ID,\n" +
                        "                        PREV_PHOTO_LAYER,\n" +
                        "                        FLOWB_ID,\n" +
                        "                        CJ_ID,\n" +
                        "                        REWORK_COUNT,\n" +
                        "                        ORIG_WAFER_QTY,\n" +
                        "                        CUR_WAFER_QTY,\n" +
                        "                        PROD_WAFER_QTY,\n" +
                        "                        NPW_WAFER_QTY,\n" +
                        "                        CLAIM_PROD_QTY,\n" +
                        "                        TRX_NPW_QTY,\n" +
                        "                        TOTAL_GOOD_UNIT,\n" +
                        "                        TOTAL_FAIL_UNIT,\n" +
                        "                        LOT_OWNER_ID,\n" +
                        "                        PLAN_END_TIME,\n" +
                        "                        WAFER_HIS_TIME,\n" +
                        "                        CRITERIA_FLAG,\n" +
                        "                        TRX_MEMO,\n" +
                        "                        RPARAM_CHG_TYPE,      \n" +
                        "                STORE_TIME,\n" +
                        "                        RELATED_LOT_ID,         \n" +
                        "                BOND_GRP_ID,             \n" +
                        "                DPT_NAME_PLATE,            \n" +
                        "                MON_GRP_ID             \n" +
                        "                from  OHLOTOPE\n" +
                        "                where LOT_ID       = ?         and\n" +
                        "                TRX_TIME        >= ?\n";


                //add fromTimeStamp and toTimeStamp for history query
                sqlTemp = "";
                if (CimStringUtils.isNotEmpty( fromTimeStamp )) {
                    sqlTemp = String.format(" AND EVENT_CREATE_TIME >= TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", fromTimeStamp);
                    sql += sqlTemp ;
                }
                if (CimStringUtils.isNotEmpty( toTimeStamp )) {
                    sqlTemp = String.format(" AND EVENT_CREATE_TIME <= TO_TIMESTAMP('%s', 'yyyy-mm-dd hh24:mi:ss.ff')", toTimeStamp);
                    sql += sqlTemp ;
                }

                sql += "                order by TRX_TIME, EVENT_CREATE_TIME, STORE_TIME";

                List<Object[]> v31__160=cimJpaRepository.query(sql,lvhFHOPEHSLOT_ID,
                        lvhFHOPEHSCLAIM_TIME);


                Long t_len1 = 1000L;
                Long count1 = 0L;
                strOperationHisInfo=new ArrayList<>();

                strOperationHistoryFillInTxPLQ008DROut.setStrOperationHisInfo(strOperationHisInfo);

                for (Object[] obj:v31__160) {
                    Object hFHOPEHSLOT_ID = obj[0];
                    Object hFHOPEHSLOT_TYPE = obj[1];
                    Object hFHOPEHSCAST_ID = obj[2];
                    Object hFHOPEHSCAST_CATEGORY = obj[3];
                    Object hFHOPEHSMAINPD_ID = obj[4];
                    Object hFHOPEHSOPE_NO = obj[5];
                    Object hFHOPEHSPD_ID = obj[6];
                    Object hFHOPEHSPD_TYPE = obj[7];
                    Object hFHOPEHSOPE_PASS_COUNT = obj[8];
                    Object hFHOPEHSPD_NAME = obj[9];
                    Object hFHOPEHSHOLD_STATE = obj[10];
                    Object hFHOPEHSCLAIM_TIME = obj[11];
                    Object hFHOPEHSCLAIM_USER_ID = obj[12];
                    Object hFHOPEHSMOVE_TYPE = obj[13];
                    Object hFHOPEHSOPE_CATEGORY = obj[14];
                    Object hFHOPEHSPROD_TYPE = obj[15];
                    Object hFHOPEHSTEST_TYPE = obj[16];
                    Object hFHOPEHSMFG_LAYER = obj[17];
                    Object hFHOPEHSPRIORITY_CLASS = obj[18];
                    Object hFHOPEHSPREV_PRODSPEC_ID = obj[19];
                    Object hFHOPEHSPRODSPEC_ID = obj[20];
                    Object hFHOPEHSPRODGRP_ID = obj[21];
                    Object hFHOPEHSTECH_ID = obj[22];
                    Object hFHOPEHSCUSTOMER_ID = obj[23];
                    Object hFHOPEHSCUSTPROD_ID = obj[24];
                    Object hFHOPEHSORDER_NO = obj[25];
                    Object hFHOPEHSSTAGE_ID = obj[26];
                    Object hFHOPEHSSTAGEGRP_ID = obj[27];
                    Object hFHOPEHSPHOTO_LAYER = obj[28];
                    Object hFHOPEHSLOCATION_ID = obj[29];
                    Object hFHOPEHSAREA_ID = obj[30];
                    Object hFHOPEHSEQP_ID = obj[31];
                    Object hFHOPEHSEQP_NAME = obj[32];
                    Object hFHOPEHSOPE_MODE = obj[33];
                    Object hFHOPEHSLC_RECIPE_ID = obj[34];
                    Object hFHOPEHSRECIPE_ID = obj[35];
                    Object hFHOPEHSPH_RECIPE_ID = obj[36];
                    Object hFHOPEHSRETICLE_COUNT = obj[37];
                    Object hFHOPEHSFIXTURE_COUNT = obj[38];
                    Object hFHOPEHSRPARM_COUNT = obj[39];
                    Object hFHOPEHSINIT_HOLD_FLAG = obj[40];
                    Object hFHOPEHSLAST_HLDREL_FLAG = obj[41];
                    Object hFHOPEHSHOLD_TIME = obj[42];
                    Object hFHOPEHSHOLD_USER_ID = obj[43];
                    Object hFHOPEHSHOLD_TYPE = obj[44];
                    Object hFHOPEHSHOLD_REASON_CODE = obj[45];
                    Object hFHOPEHSHOLD_REASON_DESC = obj[46];
                    Object hFHOPEHSREASON_CODE = obj[47];
                    Object hFHOPEHSREASON_DESCRIPTION = obj[48];
                    Object hFHOPEHSBANK_ID = obj[49];
                    Object hFHOPEHSPREV_BANK_ID = obj[50];
                    Object hFHOPEHSPREV_MAINPD_ID = obj[51];
                    Object hFHOPEHSPREV_OPE_NO = obj[52];
                    Object hFHOPEHSPREV_PD_ID = obj[53];
                    Object hFHOPEHSPREV_PD_TYPE = obj[54];
                    Object hFHOPEHSPREV_PD_NAME = obj[55];
                    Object hFHOPEHSPREV_PASS_COUNT = obj[56];
                    Object hFHOPEHSPREV_STAGE_ID = obj[57];
                    Object hFHOPEHSPREV_STAGEGRP_ID = obj[58];
                    Object hFHOPEHSPREV_PHOTO_LAYER = obj[59];
                    Object hFHOPEHSFLOWBATCH_ID = obj[60];
                    Object hFHOPEHSCTRL_JOB = obj[61];
                    Object hFHOPEHSREWORK_COUNT = obj[62];
                    Object hFHOPEHSORG_WAFER_QTY = obj[63];
                    Object hFHOPEHSCUR_WAFER_QTY = obj[64];
                    Object hFHOPEHSPROD_WAFER_QTY = obj[65];
                    Object hFHOPEHSCNTL_WAFER_QTY = obj[66];
                    Object hFHOPEHSCLAIM_PROD_QTY = obj[67];
                    Object hFHOPEHSCLAIM_CNTL_QTY = obj[68];
                    Object hFHOPEHSTOTAL_GOOD_UNIT = obj[69];
                    Object hFHOPEHSTOTAL_FAIL_UNIT = obj[70];
                    Object hFHOPEHSLOT_OWNER_ID = obj[71];
                    Object hFHOPEHSPLAN_END_TIME = obj[72];
                    Object hFHOPEHSWFRHS_TIME = obj[73];
                    Object hFHOPEHSCRITERIA_FLAG = obj[74];
                    Object hFHOPEHSCLAIM_MEMO = obj[75];
                    Object hFHOPEHSRPARM_CHANGE_TYPE = obj[76];
                    Object hFHOPEHSSTORE_TIME = obj[77];
                    Object hFHOPEHSRELATED_LOT_ID = obj[78];
                    Object hFHOPEHSBOND_GRP_ID = obj[79];
                    Object hDPT_NAME_PLATE = obj[80];
                    Object hMON_GRP_ID = obj[81];


                    Infos.OperationHisInfo operationHisInfo=new Infos.OperationHisInfo();
                    if( count1 >= t_len1 ) {
                        t_len1 = t_len1 + 500;
                    }

                    if ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION) ) ||
                            ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE) ) ) {

                        if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_SCHEDULECHANGE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLIT) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL) ) ) {

                            continue;
                        } else {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPHOTO_LAYER));
                        }
                    } else if ( ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION) ) ||
                            ( CimStringUtils.equals((String) hFHOPEHSMOVE_TYPE,BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE) ) ) {

                        if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOTHOLDRELEASE) )      ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_OPERATIONCOMPLETE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_FORCECOMP) ) ||    //P5000252
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_OPECOMPPARTIAL) ) ||    //DSN000015229
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_GATEPASS) )  ) {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSPREV_MAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSPREV_OPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSPREV_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPREV_PD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPREV_PD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPREV_PD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSPREV_STAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSPREV_STAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPREV_PHOTO_LAYER));
                        } else if ( ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_SCHEDULECHANGE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLIT) ) ||
                                ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_MOVETOSPLITCANCEL) ) ) {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPHOTO_LAYER));
                        } else {

                            continue;
                        }
                    } else {

                        if ( CimStringUtils.equals((String) hFHOPEHSOPE_CATEGORY,BizConstant.SP_OPERATIONCATEGORY_REQUEUE) ) {

                            continue;
                        } else {

                            operationHisInfo.setRouteID           (CimObjectUtils.toString(hFHOPEHSMAINPD_ID));
                            operationHisInfo.setOperationNumber   (CimObjectUtils.toString(hFHOPEHSOPE_NO));
                            operationHisInfo.setOperationPass     (CimObjectUtils.toString(hFHOPEHSOPE_PASS_COUNT));
                            operationHisInfo.setOperationID       (CimObjectUtils.toString(hFHOPEHSPD_ID));
                            operationHisInfo.setPdType            (CimObjectUtils.toString(hFHOPEHSPD_TYPE));
                            operationHisInfo.setOperationName     (CimObjectUtils.toString(hFHOPEHSPD_NAME));
                            operationHisInfo.setStageID           (CimObjectUtils.toString(hFHOPEHSSTAGE_ID));
                            operationHisInfo.setStageGroupID      (CimObjectUtils.toString(hFHOPEHSSTAGEGRP_ID));
                            operationHisInfo.setMaskLevel         (CimObjectUtils.toString(hFHOPEHSPHOTO_LAYER));
                        }
                    }


                    operationHisInfo.setReportTimeStamp           (CimObjectUtils.toString(hFHOPEHSCLAIM_TIME));
                    operationHisInfo.setOperationCategory         ((String) (hFHOPEHSOPE_CATEGORY));
                    operationHisInfo.setLocationID                ((String) (hFHOPEHSLOCATION_ID));
                    operationHisInfo.setWorkArea                  ((String) (hFHOPEHSAREA_ID));
                    operationHisInfo.setEquipmentID               ((String) (hFHOPEHSEQP_ID));
                    operationHisInfo.setEquipmentName             ((String) (hFHOPEHSEQP_NAME));
                    operationHisInfo.setOperationMode             ((String) (hFHOPEHSOPE_MODE));
                    operationHisInfo.setLogicalRecipeID           ((String) (hFHOPEHSLC_RECIPE_ID));
                    operationHisInfo.setMachineRecipeID           ((String) (hFHOPEHSRECIPE_ID));
                    operationHisInfo.setPhysicalRecipeID          ((String) (hFHOPEHSPH_RECIPE_ID));
                    operationHisInfo.setFlowBatchID               ((String) (hFHOPEHSFLOWBATCH_ID));
                    operationHisInfo.setControlJobID              ((String) (hFHOPEHSCTRL_JOB));
                    operationHisInfo.setReworkCount               (CimLongUtils.longValue(hFHOPEHSREWORK_COUNT));

                    String buffer3;
                    buffer3 = CimObjectUtils.toString(hFHOPEHSINIT_HOLD_FLAG);
                    operationHisInfo.setInitialHoldFlag           ((buffer3));

                    buffer3 = CimObjectUtils.toString(hFHOPEHSLAST_HLDREL_FLAG);
                    operationHisInfo.setLastHoldReleaseFlag       ((buffer3));

                    operationHisInfo.setHoldType                  (CimObjectUtils.toString(hFHOPEHSHOLD_TYPE));
                    operationHisInfo.setHoldTimeStamp             (CimObjectUtils.toString(hFHOPEHSHOLD_TIME));
                    operationHisInfo.setHoldUserID                (CimObjectUtils.toString(hFHOPEHSHOLD_USER_ID));
                    operationHisInfo.setHoldReasonCodeID          (CimObjectUtils.toString(hFHOPEHSHOLD_REASON_CODE));
                    operationHisInfo.setHoldReasonCodeDescription (CimObjectUtils.toString(hFHOPEHSHOLD_REASON_DESC));
                    operationHisInfo.setReasonCodeID              (CimObjectUtils.toString(hFHOPEHSREASON_CODE));
                    operationHisInfo.setReasonCodeDescription     (CimObjectUtils.toString(hFHOPEHSREASON_DESCRIPTION));
                    operationHisInfo.setBankID                    (CimObjectUtils.toString(hFHOPEHSBANK_ID));
                    operationHisInfo.setPreviousBankID            (CimObjectUtils.toString(hFHOPEHSPREV_BANK_ID));

                    operationHisInfo.setCassetteID                (CimObjectUtils.toString(hFHOPEHSCAST_ID));
                    operationHisInfo.setCassetteCategory          (CimObjectUtils.toString(hFHOPEHSCAST_CATEGORY));
                    operationHisInfo.setProductType               (CimObjectUtils.toString(hFHOPEHSPROD_TYPE));
                    operationHisInfo.setTestType                  (CimObjectUtils.toString(hFHOPEHSTEST_TYPE));
                    operationHisInfo.setPriorityClass             (CimObjectUtils.toString(hFHOPEHSPRIORITY_CLASS));
                    operationHisInfo.setProductID                 (CimObjectUtils.toString(hFHOPEHSPRODSPEC_ID));
                    operationHisInfo.setProductGroupID            (CimObjectUtils.toString(hFHOPEHSPRODGRP_ID));
                    operationHisInfo.setTechnologyID              (CimObjectUtils.toString(hFHOPEHSTECH_ID));
                    operationHisInfo.setCustomerCode              (CimObjectUtils.toString(hFHOPEHSCUSTOMER_ID));
                    operationHisInfo.setCustomerProductID         (CimObjectUtils.toString(hFHOPEHSCUSTPROD_ID));
                    operationHisInfo.setOrderNumber               (CimObjectUtils.toString(hFHOPEHSORDER_NO));
                    operationHisInfo.setLotOwnerID                (CimObjectUtils.toString(hFHOPEHSLOT_OWNER_ID));
                    operationHisInfo.setDueTimeStamp              (CimObjectUtils.toString(hFHOPEHSPLAN_END_TIME));
                    operationHisInfo.setWaferHistoryTimeStamp     (CimObjectUtils.toString(hFHOPEHSWFRHS_TIME));
                    operationHisInfo.setOriginalWaferQuantity     (CimLongUtils.longValue(hFHOPEHSORG_WAFER_QTY));
                    operationHisInfo.setCurrentWaferQuantity      (CimLongUtils.longValue(hFHOPEHSCUR_WAFER_QTY));
                    operationHisInfo.setProductWaferQuantity      (CimLongUtils.longValue(hFHOPEHSPROD_WAFER_QTY));
                    operationHisInfo.setControlWaferQuantity      (CimLongUtils.longValue(hFHOPEHSCNTL_WAFER_QTY));
                    operationHisInfo.setClaimedProductWaferQuantity (CimLongUtils.longValue(hFHOPEHSCLAIM_PROD_QTY));
                    operationHisInfo.setClaimedControlWaferQuantity (CimLongUtils.longValue(hFHOPEHSCLAIM_CNTL_QTY));
                    operationHisInfo.setTotalGoodUnit             (CimLongUtils.longValue(hFHOPEHSTOTAL_GOOD_UNIT));
                    operationHisInfo.setTotalFailUnit             (CimLongUtils.longValue(hFHOPEHSTOTAL_FAIL_UNIT));

                    operationHisInfo.setTestCriteriaFlag          (CimBooleanUtils.convert(hFHOPEHSCRITERIA_FLAG));
                    operationHisInfo.setUserID                    (CimObjectUtils.toString(hFHOPEHSCLAIM_USER_ID));
                    operationHisInfo.setClaimMemo                 (CimObjectUtils.toString(hFHOPEHSCLAIM_MEMO));
                    operationHisInfo.setStoreTimeStamp            (CimObjectUtils.toString(hFHOPEHSSTORE_TIME));
                    operationHisInfo.setRelatedLotID              (CimObjectUtils.toString(hFHOPEHSRELATED_LOT_ID));
                    operationHisInfo.setBondingGroupID            (CimObjectUtils.toString(hFHOPEHSBOND_GRP_ID));
                    operationHisInfo.setRecipeParameterChangeType (CimObjectUtils.toString(hFHOPEHSRPARM_CHANGE_TYPE));
                    operationHisInfo.setMonitorGroupId ((String) (hMON_GRP_ID));



                    String departmentNamePlate = (String) (hDPT_NAME_PLATE);
                    if (CimStringUtils.isNotEmpty(departmentNamePlate)) {
                        List<String> departmentAndSection = Arrays.stream(departmentNamePlate.split("\\.", 2)).collect(Collectors.toList());
                        if (CimNumberUtils.eq(departmentAndSection.size(), 2)) {
                            operationHisInfo.setDepartment(departmentAndSection.get(0));
                            operationHisInfo.setSection(departmentAndSection.get(1));
                        }
                    }

                    sql="select RTCL_ID,LOT_ID\n" +
                            "                    from   OHLOTOPE_RTCL\n" +
                            "                    where LOT_ID       = ?    and\n" +
                            "                    PROCESS_ID          = ?    and\n" +
                            "                    OPE_NO             = ?    and\n" +
                            "                    OPE_PASS_COUNT     = ?    and\n" +
                            "                    TRX_TIME         = ?    and\n" +
                            "                    OPE_CATEGORY       = ?";

                    List<Object[]> v32__160=cimJpaRepository.query(sql,hFHOPEHSLOT_ID
                            ,hFHOPEHSMAINPD_ID
                            ,hFHOPEHSOPE_NO
                            ,hFHOPEHSOPE_PASS_COUNT
                            ,hFHOPEHSCLAIM_TIME
                            ,hFHOPEHSOPE_CATEGORY );

                    Long t_len2 = nReticleSeqLen;
                    Long count2 = 0L;
                    List<Infos.OpeHisReticleInfo> strOpeHisReticleInfo;
                    strOpeHisReticleInfo=new ArrayList<>();
                    operationHisInfo.setStrOpeHisReticleInfo(strOpeHisReticleInfo);

                    for (Object[] object:v32__160) {
                        Object hFHOPEHS_RETICLERETICLE_ID = object[0];


                        if( count2 >= t_len2 ) {
                            t_len2 = t_len2 + nReticleSeqLen;
                        }
                        Infos.OpeHisReticleInfo opeHisReticleInfo=new Infos.OpeHisReticleInfo();
                        strOpeHisReticleInfo.add(opeHisReticleInfo);
                        opeHisReticleInfo.setReticleID ((String) (hFHOPEHS_RETICLERETICLE_ID));
                        count2++;
                    }


                    sql="select RTCL_ID,LOT_ID\n" +
                            "                    from   OHLOTOPE_FIXT\n" +
                            "                    where LOT_ID       = ?     and\n" +
                            "                    PROCESS_ID          = ?     and\n" +
                            "                    OPE_NO             = ?     and\n" +
                            "                    OPE_PASS_COUNT     = ?     and\n" +
                            "                    TRX_TIME         = ?     and\n" +
                            "                    OPE_CATEGORY       = ?";

                    List<Object[]> v33__160=cimJpaRepository.query(sql,hFHOPEHSLOT_ID
                            ,hFHOPEHSMAINPD_ID
                            ,hFHOPEHSOPE_NO
                            ,hFHOPEHSOPE_PASS_COUNT
                            ,hFHOPEHSCLAIM_TIME
                            ,hFHOPEHSOPE_CATEGORY );


                    Long t_len3 = nFixtureSeqLen;
                    Long count3 = 0L;
                    List<Infos.OpeHisFixtureInfo> strOpeHisFixtureInfo=new ArrayList<>();
                    operationHisInfo.setStrOpeHisFixtureInfo(strOpeHisFixtureInfo);

                    for (Object[] object:v33__160) {
                        Object hFHOPEHS_FIXTUREFIXTURE_ID=object[0];


                        if( count3 >= t_len3 ) {
                            t_len3 = t_len3 + nFixtureSeqLen;
                        }
                        Infos.OpeHisFixtureInfo opeHisFixtureInfo=new Infos.OpeHisFixtureInfo();
                        strOpeHisFixtureInfo.add(opeHisFixtureInfo);
                        opeHisFixtureInfo.setFixtureID ((String) (hFHOPEHS_FIXTUREFIXTURE_ID));
                        count3++;
                    }



                    Infos.LotOpeHisRParmGetDROut strLotOpeHisRParmGetDROut;
                    // step3 - lot_opeHisRParm_GetDR
                    strLotOpeHisRParmGetDROut = lotMethod.lotOpeHisRParmGetDR(strObjCommonIn,
                            (String) hFHOPEHSLOT_ID,
                            (String) hFHOPEHSMAINPD_ID,
                            (String) hFHOPEHSOPE_NO,
                            CimLongUtils.longValue(hFHOPEHSOPE_PASS_COUNT),
                            CimObjectUtils.toString(hFHOPEHSCLAIM_TIME),
                            (String) hFHOPEHSOPE_CATEGORY);

                    if ( CimStringUtils.equals(BizConstant.SP_RPARM_CHANGETYPE_BYWAFER, (String) hFHOPEHSRPARM_CHANGE_TYPE) ) {
                        operationHisInfo.setStrOpeHisRecipeParmInfo (strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmInfo());
                        operationHisInfo.setStrOpeHisRecipeParmWaferInfo (strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmWaferInfo());
                    } else {
                        operationHisInfo.setStrOpeHisRecipeParmInfo (strLotOpeHisRParmGetDROut.getStrOpeHisRecipeParmInfo());
                        operationHisInfo.setStrOpeHisRecipeParmWaferInfo(new ArrayList<>());
                    }

                    strOperationHisInfo.add(operationHisInfo);
                    count1++;

                    if ( count1 >= lenOpeHisMax ) {
                        break;
                    }
                }
            }
        }
        return strOperationHistoryFillInTxPLQ008DROut;
    }

    @Override
    public Outputs.ObjOperationStartLotCountByLoadPurposeTypeOut operationStartLotLotCountGetByLoadPurposeType(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList) {
        log.debug("getOperationStartLotCountByLoadPurposeType(): enter getOperationStartLotCountByLoadPurposeType");
        Outputs.ObjOperationStartLotCountByLoadPurposeTypeOut data = new Outputs.ObjOperationStartLotCountByLoadPurposeTypeOut();
        //Prepare Each Sequence of Return Structure
        data.setEmptyCassetteIDs(new ArrayList<>());
        data.setProcessLotIDs(new ArrayList<>());
        data.setStartCassetteIDs(new ArrayList<>());

        AtomicReference<Long> scCnt = new AtomicReference<>(0L);
        AtomicReference<Long> ecCnt = new AtomicReference<>(0L);
        AtomicReference<Long> plCnt = new AtomicReference<>(0L);
        AtomicReference<Long> pmCnt = new AtomicReference<>(0L);
        Predicate<Infos.LotInCassette> operationStartFilter = lotInCassette -> lotInCassette.getMoveInFlag() != null && lotInCassette.getMoveInFlag();
        startCassetteList.forEach(startCassette -> {
            String loadPurposeType = startCassette.getLoadPurposeType();
            if (CimArrayUtils.generateList(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT,
                    BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT,
                    BizConstant.SP_LOADPURPOSETYPE_OTHER).contains(loadPurposeType)) {
                scCnt.set(scCnt.get() + 1);
                data.getStartCassetteIDs().add(startCassette.getCassetteID());
            }
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, loadPurposeType)) {
                ecCnt.set(ecCnt.get() + 1);
                data.getEmptyCassetteIDs().add(startCassette.getCassetteID());
            }
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (!CimArrayUtils.isEmpty(lotInCassetteList)){
                lotInCassetteList.stream()
                        .filter(operationStartFilter)
                        .forEach(lotInCassette -> {
                            Boolean monitorLotFlag = lotInCassette.getMonitorLotFlag();
                            if (monitorLotFlag != null && monitorLotFlag) {
                                pmCnt.set(pmCnt.get() + 1);
                                data.setProcessMonitorLotID(lotInCassette.getLotID());
                            } else {
                                plCnt.set(plCnt.get() + 1);
                                data.getProcessLotIDs().add(lotInCassette.getLotID());
                            }
                        });
            }

        });
        //Check ProcessMonitor lot's Count
        Validations.check(pmCnt.get() > 1, retCodeConfig.getInvalidProductMonitorCount(), objCommon.getTransactionID());
        return data;
    }

    @Override
    public Outputs.ObjOperationPdTypeGetOut operationPdTypeGet(Infos.ObjCommon objCommon, ObjectIdentifier operationID) {
        Outputs.ObjOperationPdTypeGetOut data = new Outputs.ObjOperationPdTypeGetOut();
        CimProcessDefinition cimProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class, operationID);
        Validations.check(CimObjectUtils.isEmpty(cimProcessDefinition), retCodeConfig.getNotFoundProcessDefinition());
        data.setOperationName(cimProcessDefinition.getProcessDefinitionName());
        data.setPdType(cimProcessDefinition.getProcessDefinitionType());
        return data;
    }

    @Override
    public List<Infos.StartCassette> operationStartCassetteSet(Infos.ObjCommon objCommon, List<Infos.StartCassette> strStartCassette) {
        int stacastLen = CimArrayUtils.getSize(strStartCassette);
        for (int j = 0; j < stacastLen; j++){
            Infos.StartCassette startCassette = strStartCassette.get(j);
            int lotincastLen = CimArrayUtils.getSize(startCassette.getLotInCassetteList());
            for (int i=0; i<lotincastLen; i++){
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(i);
                if (!lotInCassette.getMoveInFlag()){
                    log.info("strStartCassette.strLotInCassette.operationFlag = FALSE");
                    continue;
                }
                /*---------------------------*/
                /*   Set strStartOperation   */
                /*---------------------------*/
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
                CimProcessOperation aPO = null;
                if (aLot != null){
                    aPO = aLot.getProcessOperation();
                }
                if (aPO != null){
                    /*-----------------*/
                    /*   Set routeID   */
                    /*-----------------*/
                    CimProcessDefinition aMainPD = aLot.getMainProcessDefinition();
                    Validations.check(aMainPD == null, new OmCode(retCodeConfig.getNotFoundRoute(), ""));
                    if (null == lotInCassette.getStartOperationInfo()){
                        lotInCassette.setStartOperationInfo(new Infos.StartOperationInfo());
                    }
                    lotInCassette.getStartOperationInfo().setProcessFlowID(new ObjectIdentifier(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                    /*---------------------*/
                    /*   Set operationID   */
                    /*---------------------*/
                    CimProcessDefinition aPD = aPO.getProcessDefinition();
                    Validations.check(aPD == null, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
                    lotInCassette.getStartOperationInfo().setOperationID(new ObjectIdentifier(aPD.getIdentifier(), aPD.getPrimaryKey()));
                    /*-------------------------*/
                    /*   Set operationNumber   */
                    /*-------------------------*/
                    lotInCassette.getStartOperationInfo().setOperationNumber(aLot.getOperationNumber());
                    /*-------------------*/
                    /*   Set passCount   */
                    /*-------------------*/
                    lotInCassette.getStartOperationInfo().setPassCount(CimNumberUtils.intValue(aPO.getPassCount()));
                }
            }
        }
        return strStartCassette;
    }

    @Override
    public Boolean getAndCheckBackSideCleanCarrierExchangeByFlowStep(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList) {
        Boolean reCarrierExchangeFlag = true;
        List<Infos.StartCassette> productCarrierList = startCassetteList.stream().filter(startCassette -> !CimStringUtils.equals(startCassette.getLoadPurposeType(),BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)).collect(Collectors.toList());
        if (CimArrayUtils.isNotEmpty(productCarrierList)){
            for (Infos.StartCassette productCarrier : productCarrierList) {
                //check productCarrier lot List
                if (CimArrayUtils.isNotEmpty(productCarrier.getLotInCassetteList())){
                    for (Infos.LotInCassette lotInCassette : productCarrier.getLotInCassetteList()) {

                        //不在同一个step的时候不需要判断未加工的lot
                        if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) continue;

                        ObjectIdentifier lotID = lotInCassette.getLotID();
                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
                        Validations.check(null == aLot,retCodeConfig.getNotFoundLot(),ObjectIdentifier.fetchValue(lotID));
                        //get lot process step currentStepCarrierCategory and nextStepCarrierCarrierCategory
                        String currentProcessOperationRequiredCarrierCategory = aLot.getRequiredCassetteCategory();
                        String nextProcessOperationRequiredCassetteCategory = lotMethod.lotRequiredCassetteCategoryGetForNextOperation(objCommon, lotID);
                        //case1: A -> B same no change
                        if (CimStringUtils.isNotEmpty(currentProcessOperationRequiredCarrierCategory) &&
                                CimStringUtils.isNotEmpty(nextProcessOperationRequiredCassetteCategory) &&
                                CimStringUtils.equals(currentProcessOperationRequiredCarrierCategory,nextProcessOperationRequiredCassetteCategory)){
                            reCarrierExchangeFlag = false;
                            break;
                        }
                        //case2: A -> null no change
                        if (CimStringUtils.isNotEmpty(currentProcessOperationRequiredCarrierCategory) && CimStringUtils.isEmpty(nextProcessOperationRequiredCassetteCategory)){
                            reCarrierExchangeFlag = false;
                            break;
                        }
                        //case3: null -> null no change
                        if (CimStringUtils.isEmpty(currentProcessOperationRequiredCarrierCategory) && CimStringUtils.isEmpty(nextProcessOperationRequiredCassetteCategory)){
                            reCarrierExchangeFlag = false;
                            break;
                        }
                        //case4: null -> B
                        String backNextProcessOperationRequiredCassetteCategory = null;
                        if (CimStringUtils.isEmpty(currentProcessOperationRequiredCarrierCategory) && CimStringUtils.isNotEmpty(nextProcessOperationRequiredCassetteCategory)){
                            if (CimStringUtils.isNotEmpty(backNextProcessOperationRequiredCassetteCategory)){
                                //not the first time fetch the nextProcessOperationRequiredCassetteCategory
                                if (!CimStringUtils.equals(backNextProcessOperationRequiredCassetteCategory,nextProcessOperationRequiredCassetteCategory)){
                                    reCarrierExchangeFlag = false;
                                    break;
                                }
                            }
                            backNextProcessOperationRequiredCassetteCategory = nextProcessOperationRequiredCassetteCategory;
                            //check product carrier's category is fetch next 'step' carrier category
                            ObjectIdentifier cassetteID = productCarrier.getCassetteID();
                            CimCassette productCarrierBo = baseCoreFactory.getBO(CimCassette.class, cassetteID);
                            Validations.check(null == productCarrierBo,retCodeConfig.getNotFoundCassette(),ObjectIdentifier.fetchValue(cassetteID));
                            String productCarrierCassetteCategory = productCarrierBo.getCassetteCategory();
                            if (CimStringUtils.equals(productCarrierCassetteCategory,nextProcessOperationRequiredCassetteCategory)){
                                reCarrierExchangeFlag = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return reCarrierExchangeFlag;
    }
}
