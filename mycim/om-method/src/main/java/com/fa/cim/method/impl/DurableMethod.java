package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.entity.nonruntime.postprocess.CimPostProcessDO;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.cassette.CimCassetteHoldRecordDO;
import com.fa.cim.entity.runtime.durable.CimDurableDO;
import com.fa.cim.entity.runtime.durable.CimDurableHoldRecordDO;
import com.fa.cim.entity.runtime.durablectrljob.CimDurableControlJobDO;
import com.fa.cim.entity.runtime.durablectrljob.CimDurableControlJobDurableDO;
import com.fa.cim.entity.runtime.durablegroup.CimDurableGroupDO;
import com.fa.cim.entity.runtime.durablesubstate.CimDurableSubStateDO;
import com.fa.cim.entity.runtime.durablesubstate.CimDurableSubStateNextDO;
import com.fa.cim.entity.runtime.durablesubstate.CimDurableSubStateSltDO;
import com.fa.cim.entity.runtime.reticlepod.CimReticlePodDO;
import com.fa.cim.entity.runtime.reticlepod.CimReticlePodHoldRecordDO;
import com.fa.cim.entitysuper.DurableDO;
import com.fa.cim.entitysuper.DurableHoldRecordDO;
import com.fa.cim.enums.MethodEnums;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.code.CimCategory;
import com.fa.cim.newcore.bo.code.CimCode;
import com.fa.cim.newcore.bo.code.CodeManager;
import com.fa.cim.newcore.bo.dispatch.DispatchingManager;
import com.fa.cim.newcore.bo.durable.*;
import com.fa.cim.newcore.bo.factory.CimBank;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.pd.CimDurableProcessOperation;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.durable.DurableDTO;
import com.fa.cim.newcore.dto.global.GlobalDTO;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.framework.jpa.CoreJpaRepository;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.Durable;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.mchnmngm.PortResource;
import com.fa.cim.newcore.standard.mchnmngm.ProcessResource;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.fa.cim.common.constant.BizConstant.*;
import static com.fa.cim.constant.HistoryInfoConstant.DurableConstant.*;

/**
 * description:
 * DurableCompImpl .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/30        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/7/30 11:06
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class DurableMethod implements IDurableMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IStockerMethod stockerMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    @Qualifier("CodeManagerCore")
    private CodeManager codeManager;

    @Autowired
    @Qualifier("PersonManagerCore")
    private PersonManager personManager;

    @Autowired
    @Qualifier("DispatchingManagerCore")
    private DispatchingManager dispatchingManager;

    @Autowired
    @Qualifier("DurableManagerCore")
    private DurableManager durableManager;

    @Autowired
    private CoreJpaRepository cimJpaRepository;

    @Autowired
    private ISorterMethod sorterMethod;

    @Autowired
    private IPostProcessMethod postProcessMethod;

    @Autowired
    private LotMethod lotMethod;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param theTblName
     * @param theSelectItemDefinition
     * @param strTableRecordInfoSeq
     * @param recordInfCount
     * @return int
     * @throws
     * @author Ho
     * @date 2019/4/30 17:41
     */
    private Long SET_RECORD_INFO(String theTblName, String[][] theSelectItemDefinition, List<Infos.TableRecordInfo> strTableRecordInfoSeq, Long recordInfCount) {
        int columnCnt = 0;
        Infos.TableRecordInfo strTableRecordInfo = new Infos.TableRecordInfo();
        if (CimArrayUtils.getSize(strTableRecordInfoSeq) > recordInfCount) {
            strTableRecordInfo = strTableRecordInfoSeq.get(recordInfCount.intValue());
        } else {
            strTableRecordInfoSeq.add(strTableRecordInfo);
        }
        strTableRecordInfo.setTableName(theTblName);
        List<String> columnNames = new ArrayList<>();
        strTableRecordInfo.setColumnNames(columnNames);

        for (int defIndex = 0; defIndex < SELECT_ITEM_MAX; defIndex++) {
            if (theSelectItemDefinition[defIndex][1] != null) {
                columnNames.add(theSelectItemDefinition[defIndex][1]);
                columnCnt++;
            }
        }

        recordInfCount++;
        return recordInfCount;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param theTblName
     * @param theInParamTblIndex
     * @param theSelectItemDefinition
     * @param theParamKeyDefinition
     * @param strTargetTableInfoSeq
     * @param selectCnt
     * @param HV_BUFFER
     * @param HV_TMPBUFFER
     * @param HV_WHERETIMESTAMP
     * @return java.lang.Object[]
     * @throws
     * @author Ho
     * @date 2019/4/30 18:09
     */
    private Object[] GET_SELECT_SQL(String theTblName, Integer theInParamTblIndex, String[][] theSelectItemDefinition, String[][] theParamKeyDefinition, List<Infos.TargetTableInfo> strTargetTableInfoSeq, Long selectCnt, String HV_BUFFER, String HV_TMPBUFFER, String HV_WHERETIMESTAMP) {
        if (theInParamTblIndex != -1) {
            if (selectCnt == 0) {
                HV_BUFFER = "SELECT ";
            } else {
                HV_BUFFER += " UNION ALL SELECT ";
            }
            selectCnt++;

            StringBuilder HV_BUFFERBuilder = new StringBuilder(HV_BUFFER);
            for (int defIndex = 0; defIndex < SELECT_ITEM_MAX; defIndex++) {
                if (defIndex > 0) {
                    HV_BUFFERBuilder.append(",");
                }
                HV_BUFFERBuilder.append(theSelectItemDefinition[defIndex][0]);
            }
            HV_BUFFER = HV_BUFFERBuilder.toString();
            HV_TMPBUFFER = String.format(" FROM %s ", theTblName);
            HV_BUFFER += HV_TMPBUFFER;

            boolean bFirstCondition = true;

            if (CimStringUtils.length(HV_WHERETIMESTAMP) > 0) {
                bFirstCondition = false;
                HV_BUFFER += HV_WHERETIMESTAMP;
            }

            boolean bExistMandatoryKey = false;
            List<Infos.HashedInfo> strHashedInfoSeq = strTargetTableInfoSeq.get(theInParamTblIndex).getStrHashedInfoSeq();
            StringBuilder HV_BUFFERBuilder1 = new StringBuilder(HV_BUFFER);
            for (int paramIndex = 0; paramIndex < CimArrayUtils.getSize(strHashedInfoSeq); paramIndex++) {
                boolean bExistKey = false;
                int defIndex = 0;
                while (theParamKeyDefinition[defIndex][0] != null) {
                    if (CimStringUtils.equals(theParamKeyDefinition[defIndex][0], strHashedInfoSeq.get(paramIndex).getHashKey())) {
                        if (bFirstCondition) {
                            bFirstCondition = false;
                            HV_BUFFERBuilder1.append(" WHERE ");
                        } else {
                            HV_BUFFERBuilder1.append(" AND ");
                        }
                        HV_BUFFERBuilder1.append(theParamKeyDefinition[defIndex][1]);
                        HV_TMPBUFFER = String.format(" = '%s' ", (strHashedInfoSeq.get(paramIndex).getHashData()));
                        HV_BUFFERBuilder1.append(HV_TMPBUFFER);
                        bExistKey = true;

                        if (CimStringUtils.equals(MANDATORY_HASH_KEY, strHashedInfoSeq.get(paramIndex).getHashKey())) {
                            bExistMandatoryKey = true;
                        }
                        break;
                    }
                    defIndex++;
                }
                if (!bExistKey) {
                    return new Object[]{selectCnt, HV_BUFFERBuilder1.toString(), HV_TMPBUFFER, HV_WHERETIMESTAMP, retCodeConfig.getInvalidParameterWithMsg()};
                }
            }
            HV_BUFFER = HV_BUFFERBuilder1.toString();
            if (!bExistMandatoryKey) {
                return new Object[]{selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP, retCodeConfig.getInvalidParameterWithMsg()};
            }
        }
        return new Object[]{selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP, retCodeConfig.getSucc()};
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableHistory_GetDR_in
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.DurableHistoryGetDROut>
     * @throws
     * @author Ho
     * @date 2019/4/29 15:39
     */
    @Override
    public Infos.DurableHistoryGetDROut durableHistoryGetDR(Infos.ObjCommon strObjCommonIn, Infos.DurableHistoryGetDRIn strDurableHistory_GetDR_in) {

        String HV_BUFFER;
        String HV_WHERETIMESTAMP;
        String HV_TMPBUFFER;

        String hEVENT_CREATE_TIME;
        String hHISTORY_NAME;
        String hDURABLE_TYPE;
        String hACTION_CODE;
        String hDURABLE_STATUS;
        String hDRBLSUBSTATE_ID;
        String hXFER_STATUS;
        String hXFER_STAT_CHG_TIME;
        String hLOCATION;
        String hCLAIM_TIME;
        Double hCLAIM_SHOP_DATE;
        String hCLAIM_USER_ID;
        String hCLAIM_MEMO;
        String hSTORE_TIME;
        String hOPE_CATEGORY;
        String hOPE_MODE;
        String hRECIPE_ID;
        String hPH_RECIPE_ID;
        String hDCTRL_JOB;

        // add Job Status History query
        String JOB_STATUS;
        String STAT_CHG_TIME;
        String PROCESS;
        String ROUTE;
        String STEP;
        String OPE_NO;
        String EQP_ID;
        String CHAMBER_ID;


        HV_TMPBUFFER = "";
        HV_WHERETIMESTAMP = "";
        HV_BUFFER = "";

        Long extend_len = 500L;
        Long t_len = extend_len;
        Long recordInfCount = 0L;
        Long recordValCount = 0L;

        List<Infos.TableRecordInfo> strTableRecordInfoSeq = new ArrayList<>();

        List<Infos.TableRecordValue> strTableRecordValueSeq = new ArrayList<>();
        long totalSize = 0;

        String fromTimeStamp = strDurableHistory_GetDR_in.getFromTimeStamp();
        String toTimeStamp = strDurableHistory_GetDR_in.getToTimeStamp();
        Long maxRecordCount = strDurableHistory_GetDR_in.getMaxRecordCount();
        SearchCondition searchCondition = strDurableHistory_GetDR_in.getSearchCondition();
        List<Infos.TargetTableInfo> strTargetTableInfoSeq = strDurableHistory_GetDR_in.getStrTargetTableInfoSeq();
        Validations.check(CimArrayUtils.getSize(strTargetTableInfoSeq) == 0, retCodeConfig.getInvalidParameterWithMsg());

        int fhdrchs_index = -1;
        int fhdrblopehs_index = -1;
        int fhdrjobstchs_index = -1;

        for (int paramIndex = 0; paramIndex < CimArrayUtils.getSize(strTargetTableInfoSeq); paramIndex++) {

            Infos.TargetTableInfo strTargetTableInfo = strTargetTableInfoSeq.get(paramIndex);
            if (fhdrchs_index == -1 && CimStringUtils.equals(BizConstant.SP_HISTORYTABLENAME_FHDRCHS, strTargetTableInfo.getTableName())) {
                fhdrchs_index = paramIndex;
                recordInfCount = SET_RECORD_INFO(BizConstant.SP_HISTORYTABLENAME_FHDRCHS, selectItemFHDRCHS, strTableRecordInfoSeq, recordInfCount);
            } else if (fhdrblopehs_index == -1 && CimStringUtils.equals(BizConstant.SP_HISTORYTABLENAME_FHDRBLOPEHS, strTargetTableInfo.getTableName())) {
                fhdrblopehs_index = paramIndex;
                recordInfCount = SET_RECORD_INFO(BizConstant.SP_HISTORYTABLENAME_FHDRBLOPEHS, selectItemFHDRBLOPEHS, strTableRecordInfoSeq, recordInfCount);
            } else if (fhdrjobstchs_index == -1 && CimStringUtils.equals(BizConstant.SP_HISTORYTABLENAME_FHDRJOBSTCHS, strTargetTableInfo.getTableName())){
                fhdrjobstchs_index = paramIndex;
                recordInfCount = SET_RECORD_INFO(BizConstant.SP_HISTORYTABLENAME_FHDRJOBSTCHS, selectItemFHDRJOBSTCHS, strTableRecordInfoSeq, recordInfCount);
            }else {
                Validations.check(true, retCodeConfig.getInvalidParameterWithMsg());
            }
        }

        Infos.DurableHistoryGetDROut strDurableHistoryGetDROut = new Infos.DurableHistoryGetDROut();
        strDurableHistoryGetDROut.setStrTableRecordInfoSeq(strTableRecordInfoSeq);

        if (CimStringUtils.length(fromTimeStamp) > 0) {
            HV_TMPBUFFER = String.format(" WHERE EVENT_CREATE_TIME >= TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:ss')", fromTimeStamp);
            HV_WHERETIMESTAMP = HV_TMPBUFFER;
        }
        if (CimStringUtils.length(toTimeStamp) > 0) {
            if (CimStringUtils.length(HV_WHERETIMESTAMP) == 0) {
                HV_WHERETIMESTAMP = " WHERE";
            } else {
                HV_WHERETIMESTAMP += " AND";
            }
            HV_TMPBUFFER = String.format(" EVENT_CREATE_TIME <= TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:ss')", toTimeStamp);
            HV_WHERETIMESTAMP += HV_TMPBUFFER;
        }

        Long selectCnt = 0L;

        Object[] resultObject = GET_SELECT_SQL(BizConstant.SP_HISTORYTABLENAME_FHDRCHS, fhdrchs_index, selectItemFHDRCHS, paramKeyFHDRCHS, strTargetTableInfoSeq, selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP);
        Validations.check(!Validations.isSuccess((OmCode) resultObject[4]), (OmCode) resultObject[4]);

        selectCnt = (Long) resultObject[0];
        HV_BUFFER = (String) resultObject[1];
        HV_TMPBUFFER = (String) resultObject[2];
        HV_WHERETIMESTAMP = (String) resultObject[3];
        resultObject = GET_SELECT_SQL(BizConstant.SP_HISTORYTABLENAME_FHDRBLOPEHS, fhdrblopehs_index, selectItemFHDRBLOPEHS, paramKeyFHDRBLOPEHS, strTargetTableInfoSeq, selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP);
        Validations.check(!Validations.isSuccess((OmCode) resultObject[4]), (OmCode) resultObject[4]);

        selectCnt = (Long) resultObject[0];
        HV_BUFFER = (String) resultObject[1];
        HV_TMPBUFFER = (String) resultObject[2];
        HV_WHERETIMESTAMP = (String) resultObject[3];

        // add Job Status history query
        resultObject = GET_SELECT_SQL(BizConstant.SP_HISTORYTABLENAME_FHDRJOBSTCHS, fhdrjobstchs_index, selectItemFHDRJOBSTCHS, paramKeyFHDRJOBSTCHS, strTargetTableInfoSeq, selectCnt, HV_BUFFER, HV_TMPBUFFER, HV_WHERETIMESTAMP);
        Validations.check(!Validations.isSuccess((OmCode) resultObject[4]), (OmCode) resultObject[4]);


        selectCnt = (Long) resultObject[0];
        HV_BUFFER = (String) resultObject[1];
        HV_TMPBUFFER = (String) resultObject[2];
        HV_WHERETIMESTAMP = (String) resultObject[3];

        if ((CimStringUtils.length(toTimeStamp) > 0 && CimStringUtils.length(fromTimeStamp) == 0)
                || (CimStringUtils.length(toTimeStamp) == 0 && CimStringUtils.length(fromTimeStamp) == 0)) {
            HV_BUFFER += " ORDER BY EVENT_CREATE_TIME DESC";
        } else {
            HV_BUFFER += " ORDER BY EVENT_CREATE_TIME ASC";
        }

        Boolean bConvertFlag = false;
        String originalSQL = "";
        originalSQL = HV_BUFFER;

        List<Object[]> DURHISGET_C1=null;
        if (searchCondition!=null){
            Page<Object[]> queryPage = cimJpaRepository.query(HV_BUFFER, searchCondition);
            totalSize = queryPage.getTotalElements();
            DURHISGET_C1=queryPage.getContent();
        }else{
            DURHISGET_C1 = cimJpaRepository.query(HV_BUFFER);
        }


        Long entValMaxCount = StandardProperties.OM_DRBL_MAX_COUNT_FOR_HIST_INQ.getLongValue();

        Long limitCount = 0L;
        if (maxRecordCount < 1 || maxRecordCount > entValMaxCount) {
            limitCount = 100L;
        } else {
            limitCount = maxRecordCount;
        }

        if (DURHISGET_C1!=null){
            for (Object[] obj : DURHISGET_C1) {
                hEVENT_CREATE_TIME = "";
                hHISTORY_NAME = "";
                hDURABLE_TYPE = "";
                hOPE_CATEGORY = "";
                hOPE_MODE = "";
                hRECIPE_ID = "";
                hPH_RECIPE_ID = "";
                hDCTRL_JOB = "";
                hACTION_CODE = "";
                hDURABLE_STATUS = "";
                hDRBLSUBSTATE_ID = "";
                hXFER_STATUS = "";
                hXFER_STAT_CHG_TIME = "";
                hLOCATION = "";
                hCLAIM_TIME = "";
                hCLAIM_SHOP_DATE = 0D;
                hCLAIM_USER_ID = "";
                hCLAIM_MEMO = "";
                hSTORE_TIME = "";

                JOB_STATUS = "";
                STAT_CHG_TIME = "";
                PROCESS = "";
                ROUTE = "";
                STEP = "";
                OPE_NO = "";
                EQP_ID = "";
                CHAMBER_ID = "";

                hEVENT_CREATE_TIME = CimObjectUtils.toString(obj[0]);
                hHISTORY_NAME = CimObjectUtils.toString(obj[1]);
                hDURABLE_TYPE = CimObjectUtils.toString(obj[2]);
                hOPE_CATEGORY = CimObjectUtils.toString(obj[3]);
                hOPE_MODE = CimObjectUtils.toString(obj[4]);
                hRECIPE_ID = CimObjectUtils.toString(obj[5]);
                hPH_RECIPE_ID = CimObjectUtils.toString(obj[6]);
                hDCTRL_JOB = CimObjectUtils.toString(obj[7]);
                hACTION_CODE = CimObjectUtils.toString(obj[8]);
                hDURABLE_STATUS = CimObjectUtils.toString(obj[9]);
                hDRBLSUBSTATE_ID = CimObjectUtils.toString(obj[10]);
                hXFER_STATUS = CimObjectUtils.toString(obj[11]);
                hXFER_STAT_CHG_TIME = CimObjectUtils.toString(obj[12]);
                hLOCATION = CimObjectUtils.toString(obj[13]);
                hCLAIM_TIME = CimObjectUtils.toString(obj[14]);
                hCLAIM_SHOP_DATE = CimDoubleUtils.doubleValue(obj[15]);


                // add job status history query
                JOB_STATUS = CimObjectUtils.toString(obj[16]);
                STAT_CHG_TIME = CimObjectUtils.toString(obj[17]);
                PROCESS = CimObjectUtils.toString(obj[18]);
                ROUTE = CimObjectUtils.toString(obj[19]);
                STEP = CimObjectUtils.toString(obj[20]);
                OPE_NO = CimObjectUtils.toString(obj[21]);
                EQP_ID = CimObjectUtils.toString(obj[22]);
                CHAMBER_ID = CimObjectUtils.toString(obj[23]);

                hCLAIM_USER_ID = CimObjectUtils.toString(obj[24]);
                hCLAIM_MEMO = CimObjectUtils.toString(obj[25]);
                hSTORE_TIME = CimObjectUtils.toString(obj[26]);


                if (recordValCount >= t_len) {
                    t_len += extend_len;
                }

                recordValCount = SET_RECORD_VALUE(BizConstant.SP_HISTORYTABLENAME_FHDRCHS, selectItemFHDRCHS, hHISTORY_NAME, strTableRecordValueSeq, CimNumberUtils.intValue(recordValCount),
                        hEVENT_CREATE_TIME, hDURABLE_TYPE, hOPE_CATEGORY, hOPE_MODE, hRECIPE_ID, hPH_RECIPE_ID, hDCTRL_JOB, hACTION_CODE, hDURABLE_STATUS, hDRBLSUBSTATE_ID, hXFER_STATUS, hXFER_STAT_CHG_TIME, hLOCATION, hCLAIM_TIME, hCLAIM_USER_ID, hCLAIM_MEMO, hSTORE_TIME,
                        hCLAIM_SHOP_DATE,
                        JOB_STATUS,
                        STAT_CHG_TIME,
                        PROCESS,
                        ROUTE,
                        STEP,
                        OPE_NO,
                        EQP_ID,
                        CHAMBER_ID);
                recordValCount = SET_RECORD_VALUE(BizConstant.SP_HISTORYTABLENAME_FHDRBLOPEHS, selectItemFHDRBLOPEHS, hHISTORY_NAME, strTableRecordValueSeq, CimNumberUtils.intValue(recordValCount),
                        hEVENT_CREATE_TIME, hDURABLE_TYPE, hOPE_CATEGORY, hOPE_MODE, hRECIPE_ID, hPH_RECIPE_ID, hDCTRL_JOB, hACTION_CODE, hDURABLE_STATUS, hDRBLSUBSTATE_ID, hXFER_STATUS, hXFER_STAT_CHG_TIME, hLOCATION, hCLAIM_TIME, hCLAIM_USER_ID, hCLAIM_MEMO, hSTORE_TIME,
                        hCLAIM_SHOP_DATE,
                        JOB_STATUS,
                        STAT_CHG_TIME,
                        PROCESS,
                        ROUTE,
                        STEP,
                        OPE_NO,
                        EQP_ID,
                        CHAMBER_ID);

                recordValCount = SET_RECORD_VALUE(BizConstant.SP_HISTORYTABLENAME_FHDRJOBSTCHS, selectItemFHDRJOBSTCHS, hHISTORY_NAME, strTableRecordValueSeq, CimNumberUtils.intValue(recordValCount),
                        hEVENT_CREATE_TIME, hDURABLE_TYPE, hOPE_CATEGORY, hOPE_MODE, hRECIPE_ID, hPH_RECIPE_ID, hDCTRL_JOB, hACTION_CODE, hDURABLE_STATUS, hDRBLSUBSTATE_ID, hXFER_STATUS, hXFER_STAT_CHG_TIME, hLOCATION, hCLAIM_TIME, hCLAIM_USER_ID, hCLAIM_MEMO, hSTORE_TIME,
                        hCLAIM_SHOP_DATE,
                        JOB_STATUS,
                        STAT_CHG_TIME,
                        PROCESS,
                        ROUTE,
                        STEP,
                        OPE_NO,
                        EQP_ID,
                        CHAMBER_ID);

                if (searchCondition==null) {
                    if (recordValCount > limitCount) {
                        recordValCount = limitCount;
                        break;
                    }
                }
            }
        }

        strDurableHistoryGetDROut.setStrTableRecordValueSeq(strTableRecordValueSeq);
        if (searchCondition!=null) {
            strDurableHistoryGetDROut.setStrTableRecordValuePage(
                    CimPageUtils.convertListToPage(strTableRecordValueSeq,searchCondition.getPage(),searchCondition.getSize(),totalSize));
        }

        return strDurableHistoryGetDROut;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param theTblName
     * @param theSelectItemDefinition
     * @param hHISTORY_NAME
     * @param strTableRecordValueSeq
     * @param recordValCount
     * @param hEVENT_CREATE_TIME
     * @param hDURABLE_TYPE
     * @param hOPE_CATEGORY
     * @param hOPE_MODE
     * @param hRECIPE_ID
     * @param hPH_RECIPE_ID
     * @param hDCTRL_JOB
     * @param hACTION_CODE
     * @param hDURABLE_STATUS
     * @param hDRBLSUBSTATE_ID
     * @param hXFER_STATUS
     * @param hXFER_STAT_CHG_TIME
     * @param hLOCATION
     * @param hCLAIM_TIME
     * @param hCLAIM_USER_ID
     * @param hCLAIM_MEMO
     * @param hSTORE_TIME
     * @param hCLAIM_SHOP_DATE
     * @return java.lang.Long
     * @throws
     * @author Ho
     * @date 2019/5/5 16:32
     */
    public Long SET_RECORD_VALUE(String theTblName, String[][] theSelectItemDefinition,
                                 String hHISTORY_NAME, List<Infos.TableRecordValue> strTableRecordValueSeq, Integer recordValCount,
                                 String hEVENT_CREATE_TIME, String hDURABLE_TYPE, String hOPE_CATEGORY,
                                 String hOPE_MODE, String hRECIPE_ID, String hPH_RECIPE_ID, String hDCTRL_JOB, String hACTION_CODE, String hDURABLE_STATUS, String hDRBLSUBSTATE_ID, String hXFER_STATUS, String hXFER_STAT_CHG_TIME, String hLOCATION, String hCLAIM_TIME, String hCLAIM_USER_ID, String hCLAIM_MEMO, String hSTORE_TIME,
                                 Double hCLAIM_SHOP_DATE,
                                 String JOB_STATUS,
                                 String STAT_CHG_TIME,
                                 String PROCESS,
                                 String ROUTE,
                                 String STEP,
                                 String OPE_NO,
                                 String EQP_ID,
                                 String CHAMBER_ID) {
        if (CimStringUtils.equals(theTblName, hHISTORY_NAME)) {
            StringBuffer stringBuffer = new StringBuffer();
            int defIndex = 0;
            int columnCnt = 0;
            List<Object> columnValues = new ArrayList<>();
            Infos.TableRecordValue strTableRecordValue = new Infos.TableRecordValue();
            strTableRecordValueSeq.add(strTableRecordValue);
            strTableRecordValue.setColumnValues(columnValues);

            if (theSelectItemDefinition[defIndex++][1] != null) {
                strTableRecordValue.setReportTimeStamp(hEVENT_CREATE_TIME);
                columnValues.add(hEVENT_CREATE_TIME);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                strTableRecordValue.setTableName(hHISTORY_NAME);
                columnValues.add(hHISTORY_NAME);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hDURABLE_TYPE);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hOPE_CATEGORY);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hOPE_MODE);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hRECIPE_ID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hPH_RECIPE_ID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hDCTRL_JOB);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hACTION_CODE);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hDURABLE_STATUS);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hDRBLSUBSTATE_ID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hXFER_STATUS);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hXFER_STAT_CHG_TIME);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hLOCATION);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hCLAIM_TIME);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                stringBuffer.delete(0, stringBuffer.length());
                stringBuffer.append(hCLAIM_SHOP_DATE == null ? "" : CimDateUtils.convert("yyyy-MM-dd hh:mm:ss",new Date(hCLAIM_SHOP_DATE.longValue())));
                columnValues.add(stringBuffer.toString());
                columnCnt++;
            }

            // add clean job status history query  -- start --
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(JOB_STATUS);
                columnCnt++;
            }

            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(STAT_CHG_TIME);
                columnCnt++;
            }

            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(PROCESS);
                columnCnt++;
            }

            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(ROUTE);
                columnCnt++;
            }

            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(STEP);
                columnCnt++;
            }

            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(OPE_NO);
                columnCnt++;
            }

            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(EQP_ID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(CHAMBER_ID);
                columnCnt++;
            }
            // add clean job status history query  -- end --


            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hCLAIM_USER_ID);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hCLAIM_MEMO);
                columnCnt++;
            }
            if (theSelectItemDefinition[defIndex++][1] != null) {
                columnValues.add(hSTORE_TIME);
                columnCnt++;
            }

            recordValCount++;
        }
        return recordValCount.longValue();
    }

    @Override
    public ObjectIdentifier durableDurableControlJobIDGet(Infos.ObjCommon objCommon, ObjectIdentifier durableID, String durableCategory) {
        com.fa.cim.newcore.bo.durable.CimDurableControlJob aDurableControlJob = null;
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), durableID.getValue()));
            aDurableControlJob = aCassette.getDurableControlJob();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(aReticlePod == null, new OmCode(retCodeConfig.getNotFoundReticlePod(), durableID.getValue()));
            aDurableControlJob = aReticlePod.getDurableControlJob();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(aDurable == null, new OmCode(retCodeConfig.getNotFoundReticle(), durableID.getValue()));
            aDurableControlJob = aDurable.getDurableControlJob();
        } else {
            // normal case. continue to the following procedure.
        }
        if (CimObjectUtils.isEmpty(aDurableControlJob)) {
            return null;
        }
        return ObjectIdentifier.build(aDurableControlJob.getIdentifier(), aDurableControlJob.getPrimaryKey());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableControlJobID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.DurableControlJobStatusGet>
     * @author Ho
     * @date 2018/10/29 15:36:37
     */
    @Override
    public Infos.DurableControlJobStatusGet durableControlJobStatusGet(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID) {

        Infos.DurableControlJobStatusGet strDurableControlJobStatusGetOut = new Infos.DurableControlJobStatusGet();

        com.fa.cim.newcore.bo.durable.CimDurableControlJob aDurableControlJob = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimDurableControlJob.class, durableControlJobID);

        // Get durable control job status
        String strControlStatus = aDurableControlJob.getControlJobStatus();
        strDurableControlJobStatusGetOut.setDurableControlJobStatus(strControlStatus);

        // Get last claimed userID
        String strTempLastClaimedUserID = aDurableControlJob.getLastClaimedUserID();
        strDurableControlJobStatusGetOut.setLastClaimedUserID(ObjectIdentifier.buildWithValue(strTempLastClaimedUserID));

        // Get last claimed timestamp
        Timestamp strTempLastClaimedTimeStamp = aDurableControlJob.getLastClaimedTimeStamp();
        strDurableControlJobStatusGetOut.setLastClaimedTimeStamp(strTempLastClaimedTimeStamp);

        return strDurableControlJobStatusGetOut;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @return com.fa.cim.dto.RetCode<java.lang.Boolean>
     * @author Ho
     * @date 2018/10/26 14:29:56
     */
    @Override
    public Boolean durableInPostProcessFlagGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        boolean isPostProcessFlagOn = false;
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), durableID.getValue()));
            isPostProcessFlagOn = aCassette.isPostProcessFlagOn();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(aReticlePod == null, new OmCode(retCodeConfig.getNotFoundReticlePod(), durableID.getValue()));
            isPostProcessFlagOn = aReticlePod.isPostProcessFlagOn();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(aReticle == null, new OmCode(retCodeConfig.getNotFoundReticle(), durableID.getValue()));
            isPostProcessFlagOn = aReticle.isPostProcessFlagOn();
        }
        return isPostProcessFlagOn;
    }

    @Override
    public List<Infos.DurableHoldRecord> durableHoldRecordGetDR(Infos.ObjCommon objCommon, ObjectIdentifier durableID, String durableCategory) {

        DurableDO drblExample;
        Class<? extends DurableHoldRecordDO> holdRcdType;

        String durableId = ObjectIdentifier.fetchValue(durableID);
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                CimCassetteDO cimCassetteExample = new CimCassetteDO();
                cimCassetteExample.setCassetteID(durableId);
                drblExample = cimCassetteExample;
                holdRcdType = CimCassetteHoldRecordDO.class;
                break;
            case SP_DURABLECAT_RETICLEPOD:
                CimReticlePodDO cimReticlePodExample = new CimReticlePodDO();
                cimReticlePodExample.setReticlePodID(durableId);
                drblExample = cimReticlePodExample;
                holdRcdType = CimReticlePodHoldRecordDO.class;
                break;
            case SP_DURABLECAT_RETICLE:
                CimDurableDO cimProcessDurableExample = new CimDurableDO();
                cimProcessDurableExample.setDurableId(durableId);
                drblExample = cimProcessDurableExample;
                holdRcdType = CimDurableHoldRecordDO.class;
                break;
            default:
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }

        return cimJpaRepository.findChildEntities(holdRcdType, Example.of(drblExample)).stream()
                .sorted(Comparator.comparing(o -> Timestamp.valueOf(o.getHoldTime())))
                .map(data -> {
                    Infos.DurableHoldRecord strDurableHoldRecord = new Infos.DurableHoldRecord();
                    strDurableHoldRecord.setD_key(data.getLinkKey());
                    strDurableHoldRecord.setHoldType(data.getHoldType());
                    strDurableHoldRecord.setHoldReasonCodeID(ObjectIdentifier.build(data.getHoldReasonID(), data.getHoldReasonObj()));
                    strDurableHoldRecord.setHoldUserID(ObjectIdentifier.build(data.getHoldUserID(), data.getHoldUserObj()));
                    strDurableHoldRecord.setHoldTime(data.getHoldTime());
                    strDurableHoldRecord.setRelatedDurableID(ObjectIdentifier.build(data.getRelatedDurableID(), data.getRelatedDurableObj()));
                    strDurableHoldRecord.setClaimMemo(data.getHoldClaimMemo());
                    strDurableHoldRecord.setResponsibleOperationMark(CimBooleanUtils.isTrue(data.getResponsibleOperationFlag()) ?
                            BizConstant.SP_RESPONSIBLEOPERATION_PREVIOUS :
                            BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    return strDurableHoldRecord;
                }).collect(Collectors.toList());
    }


    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.DurableStatusSelectionInqResult>
     * @author Ho
     * @date 2018/9/28 10:38:28
     */
    @Override
    public Results.DurableStatusSelectionInqResult durableFillInTxPDQ011DR(Infos.ObjCommon objCommon, ObjectIdentifier durableID) {
        Results.DurableStatusSelectionInqResult durableStatusSelectionInqResult = new Results.DurableStatusSelectionInqResult();
        List<Infos.CandidateDurableStatus> candidateDurableStatusList = new ArrayList<>();
        durableStatusSelectionInqResult.setStrCandidateDurableStatus(candidateDurableStatusList);

        String hFRCATEGORYCATEGORY_ID_2 = BizConstant.SP_CATEGORY_DURABLESTATE;

        com.fa.cim.newcore.bo.code.CimCategory aCategory = baseCoreFactory.getBO(com.fa.cim.newcore.bo.code.CimCategory.class, hFRCATEGORYCATEGORY_ID_2);

        String hFRCATEGORYCATEGORY_ID = aCategory.getIdentifier(), hFRDRBLDRBL_STATE = " ";

        if (!CimObjectUtils.isEmpty(aCategory)) {
            String sql = "SELECT  CODE_ID, \n" +
                    "DESCRIPTION\n" +
                    "FROM    OMCODE\n" +
                    "WHERE   CODETYPE_ID = ?1 \n" +
                    "AND CODE_ID != ?2";
            List<Object[]> codeList = cimJpaRepository.query(sql, hFRCATEGORYCATEGORY_ID_2, hFRDRBLDRBL_STATE);
            for (Object[] code : codeList) {
                Infos.CandidateDurableStatus candidateDurableStatus = new Infos.CandidateDurableStatus();
                candidateDurableStatusList.add(candidateDurableStatus);
                candidateDurableStatus.setDurableStatus((String) code[0]);
                candidateDurableStatus.setDescription((String) code[1]);
            }
            Validations.check(CimArrayUtils.isEmpty(candidateDurableStatusList), retCodeConfig.getNotFoundCode());
        } else {
            Validations.check(true, retCodeConfig.getNotFoundCategory());
        }
        return durableStatusSelectionInqResult;
    }

    @Override
    public List<Infos.DurableHoldListAttributes> durableFillInODRBQ019(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        List<DurableDTO.PosDurableHoldRecord> aHoldRecordSequence = null;
        CimCategory aCategory = null;
        CimCode aReasonCode = null;
        CimPerson aPerson = null;
        List<Infos.DurableHoldListAttributes> strDurableHoldListAttributes = new ArrayList<>();
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), durableID.getValue()));
            aHoldRecordSequence = aCassette.allHoldRecords();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(aReticlePod == null, new OmCode(retCodeConfig.getNotFoundReticlePod(), durableID.getValue()));
            aHoldRecordSequence = aReticlePod.allHoldRecords();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(aReticle == null, new OmCode(retCodeConfig.getNotFoundReticle(), durableID.getValue()));
            aHoldRecordSequence = aReticle.allHoldRecords();
        }
        int nHldRecLen = CimArrayUtils.getSize(aHoldRecordSequence);
        Validations.check(nHldRecLen == 0, retCodeConfig.getNotFoundEntry());
        aCategory = codeManager.findCategoryNamed(BizConstant.SP_REASONCAT_DURABLEHOLD);
        Validations.check(aCategory == null, retCodeConfig.getNotFoundCategory());
        for (int nHldRecSeq = 0; nHldRecSeq < nHldRecLen; nHldRecSeq++) {
            DurableDTO.PosDurableHoldRecord posDurableHoldRecord = aHoldRecordSequence.get(nHldRecSeq);
            Infos.DurableHoldListAttributes durableHoldListAttributes = new Infos.DurableHoldListAttributes();
            strDurableHoldListAttributes.add(durableHoldListAttributes);
            durableHoldListAttributes.setHoldType(posDurableHoldRecord.getHoldType());
            durableHoldListAttributes.setReasonCodeID(posDurableHoldRecord.getReasonCode());
            durableHoldListAttributes.setRelatedDurableID(posDurableHoldRecord.getRelatedDurable());
            durableHoldListAttributes.setUserID(posDurableHoldRecord.getHoldPerson());
            durableHoldListAttributes.setHoldTimeStamp(posDurableHoldRecord.getHoldTimeStamp());
            durableHoldListAttributes.setResponsibleRouteID(posDurableHoldRecord.getResponsibleRouteID());
            durableHoldListAttributes.setResponsibleOperationNumber(posDurableHoldRecord.getResponsibleOperationNumber());
            durableHoldListAttributes.setResponsibleOperationName(posDurableHoldRecord.getResponsibleOperationName());
            durableHoldListAttributes.setClaimMemo(posDurableHoldRecord.getHoldClaimMemo());
            aReasonCode = aCategory.findCodeNamed(ObjectIdentifier.fetchValue(posDurableHoldRecord.getReasonCode()));
            Validations.check(aReasonCode == null, new OmCode(retCodeConfig.getNotFoundCode(), posDurableHoldRecord.getReasonCode().getValue()));
            durableHoldListAttributes.setCodeDescription(aReasonCode.getDescription());
            if (CimStringUtils.isEmpty(posDurableHoldRecord.getHoldPerson().getReferenceKey())) {
                aPerson = personManager.findPersonNamed(posDurableHoldRecord.getHoldPerson().getValue());
            } else {
                aPerson = baseCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, posDurableHoldRecord.getHoldPerson().getReferenceKey());
            }
            if (aPerson != null) {
                durableHoldListAttributes.setUserName(aPerson.getFullName());
            } else {
                durableHoldListAttributes.setUserName("");
            }
            if (!posDurableHoldRecord.getResponsibleOperationFlag()) {
                durableHoldListAttributes.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            } else {
                durableHoldListAttributes.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_PREVIOUS);
            }
        }
        return strDurableHoldListAttributes;
    }

    /**
     * description: durable_subState_Get
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/25      *****               ZQI               update the entity name.(Drblsubst --> DurableSubState)
     *
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.pojo.Infos.DurableSubStateGetOut>
     * @author Ho
     * @date 2018/9/28 17:18:31
     */
    @Override
    public Infos.DurableSubStateGetOut durableSubStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {

        String durableStatus = "";

        com.fa.cim.newcore.bo.durable.CimDurableSubState drblsubst = null;

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette cimCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            durableStatus = cimCassette.getDurableState();
            drblsubst = cimCassette.getDurableSubState();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod cimReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            boolean isAvailableFlag = CimStringUtils.equals(BizConstant.CIMFW_DURABLE_AVAILABLE, cimReticlePod.getDurableState()),
                    isNotAvailableFlag = CimStringUtils.equals(BizConstant.CIMFW_DURABLE_NOTAVAILABLE, cimReticlePod.getDurableState()),
                    isInUseFlag = CimStringUtils.equals(BizConstant.CIMFW_DURABLE_INUSE, cimReticlePod.getDurableState()),
                    isScrappedFlag = CimStringUtils.equals(BizConstant.CIMFW_DURABLE_SCRAPPED, cimReticlePod.getDurableState());
            if (isAvailableFlag) {
                durableStatus = BizConstant.CIMFW_DURABLE_AVAILABLE;
            } else if (isNotAvailableFlag) {
                durableStatus = BizConstant.CIMFW_DURABLE_NOTAVAILABLE;
            } else if (isInUseFlag) {
                durableStatus = BizConstant.CIMFW_DURABLE_INUSE;
            } else if (isScrappedFlag) {
                durableStatus = BizConstant.CIMFW_DURABLE_SCRAPPED;
            } else {
                durableStatus = BizConstant.CIMFW_DURABLE_UNDEFINED;
            }
            drblsubst = cimReticlePod.getDurableSubState();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            CimProcessDurable cimProcessDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            if (cimProcessDurable != null) {
                durableStatus = cimProcessDurable.getDurableState();
                drblsubst = cimProcessDurable.getDurableSubState();
            }
        }

        Infos.DurableSubStateGetOut durableSubStateGetOut = new Infos.DurableSubStateGetOut();
        durableSubStateGetOut.setDurableStatus(durableStatus);
        if (drblsubst != null) {
            durableSubStateGetOut.setDurableSubStatus(new ObjectIdentifier(drblsubst.getIdentifier(), drblsubst.getPrimaryKey()));
        }

        return durableSubStateGetOut;
    }

    @Override
    public void durableOnRouteCheck(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        boolean isOnRoute = false;
        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette cimCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(cimCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), durableID.getValue()));
            isOnRoute = cimCassette.isOnRoute();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod cimReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(cimReticlePod == null, new OmCode(retCodeConfig.getNotFoundReticlePod(), durableID.getValue()));
            isOnRoute = cimReticlePod.isOnRoute();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            CimProcessDurable cimProcessDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(cimProcessDurable == null, new OmCode(retCodeConfig.getNotFoundReticle(), durableID.getValue()));
            isOnRoute = cimProcessDurable.isOnRoute();
        }

        Validations.check(isOnRoute, new OmCode(retCodeConfig.getDurableOnroute(), ObjectIdentifier.fetchValue(durableID)));
        Validations.check(!isOnRoute, new OmCode(retCodeConfig.getDurableNotOnroute(), ObjectIdentifier.fetchValue(durableID)));
    }

    /**
     * description:durable_inventoryState_Get
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param durableCategory
     * @param durableID
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Ho
     * @date 2018/9/28 16:50:47
     */
    @Override
    public String durableInventoryStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {

        String durableInventoryState = "";

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette cimCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            durableInventoryState = cimCassette.getCassetteInventoryState();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod cimReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            durableInventoryState = cimReticlePod.getDurableInventoryState();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            CimProcessDurable cimProcessDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            durableInventoryState = cimProcessDurable.getDurableInventoryState();
        }

        return durableInventoryState;
    }


    @Override
    public void durableCassetteCategoryCheckForContaminationControl(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, ObjectIdentifier equipmentID, ObjectIdentifier portID) {
        /*---------------------------------------------*/
        /* Get PosCassette Object And cassetteCategory */
        /*---------------------------------------------*/
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
        String cassetteCategory = aCassette.getCassetteCategory();
        //-----------------------------------------------------------//
        // Get PosPortResource Object And cassetteCategoryCapability //
        //-----------------------------------------------------------//
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
        PortResource aPs = aMachine.findPortResourceNamed(portID.getValue());
        Validations.check(aPs == null, retCodeConfig.getNotFoundPort(), portID.getValue());
        com.fa.cim.newcore.bo.machine.CimPortResource aPort = (com.fa.cim.newcore.bo.machine.CimPortResource) aPs;
        Validations.check(aPort == null, new OmCode(retCodeConfig.getNotFoundPortResource(), portID.getValue()));
        List<String> cassetteCategoryCapability = aPort.getCassetteCategoryCapability();
        if (!CimArrayUtils.isEmpty(cassetteCategoryCapability)) {
            //------------------------------------------------//
            // Check whether a category is the same           //
            //------------------------------------------------//
            boolean bCategoryMatch = false;
            for (String portToCasstteCapability : cassetteCategoryCapability) {
                if (CimStringUtils.equals(cassetteCategory, portToCasstteCapability)) {
                    bCategoryMatch = true;
                    break;
                }
            }
            Validations.check(!bCategoryMatch, retCodeConfig.getInvalidCategoryCheck());
        }
    }

    @Override
    public void durableCheckConditionForOperation(Infos.ObjCommon objCommon, Inputs.ObjDurableCheckConditionForOperationIn strInParam) {
        List<CimMachine> aMachineList = null;
        Boolean isBankInRequired = false;
        String durableCategory = strInParam.getDurableCategory();
        if (!BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)
                && !BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)
                && !BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
            log.info("{}", "Invalid durable category", durableCategory);
            Validations.check(true, new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }
        int sequenceCondition = StandardProperties.OM_CARRIER_LOAD_SEQ_CHK.getIntValue();
        Boolean bCassetteLoadingCheck = false;
        if (BizConstant.equalsIgnoreCase(strInParam.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
            if (sequenceCondition == 0
                    && (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)
                    || BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION))) {
                bCassetteLoadingCheck = true;
            }
            if (sequenceCondition == 1 && BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                bCassetteLoadingCheck = true;
            }
        }
        //-------------------------//
        //   Check LogicalRecipe   //
        //-------------------------//
        List<Infos.StartDurable> startDurableList = strInParam.getStartDurables();
        Infos.DurableStartRecipe durableStartRecipe = strInParam.getDurableStartRecipe();
        if (null != durableStartRecipe && !ObjectIdentifier.isEmptyWithValue(durableStartRecipe.getLogicalRecipeId())
                && (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)
                || BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART))) {
            if (!CimObjectUtils.isEmpty(startDurableList)) {
                String invalidDurableID = null;
                int invalidCnt = 0;
                for (Infos.StartDurable startDurable : startDurableList) {
                    Outputs.ObjDurableRecipeGetOut durableRecipeGetOut = this.durableRecipeGet(objCommon, durableCategory, startDurable.getDurableId(), strInParam.getEquipmentId());

                    if (!ObjectIdentifier.equalsWithValue(durableRecipeGetOut.getLogicalRecipeID(), durableStartRecipe.getLogicalRecipeId())) {
                        invalidCnt++;
                        if (CimStringUtils.isEmpty(invalidDurableID)) {
                            invalidDurableID = startDurable.getDurableId().getValue();
                        }
                    }
                }
                if (invalidCnt > 0) {
                    if (invalidCnt != startDurableList.size()) {
                        Validations.check(true, new OmCode(retCodeConfig.getNotSameLogicalRecipeForDurable(), invalidDurableID));
                    } else {
                        Validations.check(true, new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "Logical Recipe is invalid."));
                    }
                }
            }
        }
        if (!CimObjectUtils.isEmpty(startDurableList)) {
            int durableCnt = 0;
            for (Infos.StartDurable startDurable : startDurableList) {
                CimCassette aCassette = null;
                CimDurableProcessFlowContext aDurablePFX = null;
                CimReticlePod aReticlePod = null;
                CimProcessDurable aReticle = null;
                if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                    aCassette = baseCoreFactory.getBO(CimCassette.class, startDurable.getDurableId());

                    aDurablePFX = aCassette.getDurableProcessFlowContext();
                } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                    aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, startDurable.getDurableId());
                    aDurablePFX = aReticlePod.getDurableProcessFlowContext();
                } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                    aReticle = baseCoreFactory.getBO(CimProcessDurable.class, startDurable.getDurableId());
                    aDurablePFX = aReticle.getDurableProcessFlowContext();
                }
                if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_PFXCREATE)) {
                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            log.info("##### durable is on route");
                            //---------------------------------------
                            // Check Durable Inventory State
                            //---------------------------------------
                            String durableInventoryStateGetResult = this.durableInventoryStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!CimStringUtils.equals(durableInventoryStateGetResult, BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK), new OmCode(retCodeConfig.getInvalidDurableInventoryStat(), startDurable.getDurableId().getValue()));
                        }
                    }
                } else {
                    Validations.check(aDurablePFX == null, new OmCode(retCodeConfig.getNotFoundPfx(), ""));
                }
                if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)
                        || CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                    //---------------------------------------
                    //  Get InPostProcessFlag of Cassette
                    //---------------------------------------
                    Boolean durableInPostProcessFlagGet = this.durableInPostProcessFlagGet(objCommon, durableCategory, startDurable.getDurableId());
                    Validations.check(durableInPostProcessFlagGet, new OmCode(retCodeConfig.getDurableInPostProcess(), startDurable.getDurableId().getValue()));

                    //------------------------------------
                    //   Check equipment availability
                    //------------------------------------
                    equipmentMethod.equipmentCheckAvailForDurable(objCommon, strInParam.getEquipmentId());

                    if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        //-----------------------------------------------------------
                        // Check cassette interFabXferState
                        //-----------------------------------------------------------
                        String cassetteInterFabXferStateGet = cassetteMethod.cassetteInterFabXferStateGet(objCommon, startDurable.getDurableId());
                        Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, cassetteInterFabXferStateGet)
                                , new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), startDurable.getDurableId().getValue(), cassetteInterFabXferStateGet));

                        aMachineList = aCassette.getQueuedMachines();
                        /*-------------------------------*/
                        /*   Check SorterJob existence   */
                        /*-------------------------------*/
                        Infos.EquipmentLoadPortAttribute equipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
                        List<Infos.CassetteLoadPort> cassetteLoadPortList = new ArrayList<>();
                        equipmentLoadPortAttribute.setCassetteLoadPortList(cassetteLoadPortList);
                        Infos.CassetteLoadPort cassetteLoadPort = new Infos.CassetteLoadPort();
                        cassetteLoadPortList.add(cassetteLoadPort);
                        cassetteLoadPort.setPortID(startDurable.getStartDurablePort().getLoadPortID());
                        cassetteLoadPort.setCassetteID(startDurable.getDurableId());
                        equipmentLoadPortAttribute.setEquipmentID(strInParam.getEquipmentId());

                        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
                        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(equipmentLoadPortAttribute);
                        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
                        objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIDList);
                        cassetteIDList.add(startDurable.getDurableId());
                        objWaferSorterJobCheckForOperation.setLotIDList(new ArrayList<>());
                        objWaferSorterJobCheckForOperation.setOperation(strInParam.getOperation());
                        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
                        /*------------------------------------------*/
                        /*   Check Start Cassette's Loading Order   */
                        /*------------------------------------------*/
                        if (bCassetteLoadingCheck) {
                            Validations.check(strInParam.getStartDurables().get(durableCnt).getStartDurablePort().getLoadSequenceNumber() != (durableCnt + 1), new OmCode(retCodeConfig.getInvalidLoadingSequence(), startDurable.getDurableId().getValue()));
                        }
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            /*--------------------------------*/
                            /*   Get and Check ControlJobID   */
                            /*--------------------------------*/
                            CimControlJob aControlJob = aCassette.getControlJob();
                            Validations.check(aControlJob != null, retCodeConfig.getCassetteControlJobFilled());
                            /*---------------------------------------*/
                            /*   Get and Check DurableControlJobID   */
                            /*---------------------------------------*/
                            CimDurableControlJob aDurableControlJob = aCassette.getDurableControlJob();
                            Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());
                            /*----------------------------------------------*/
                            /*   Get and Check Cassette's Dispatch Status   */
                            /*----------------------------------------------*/
                            boolean dispatchReserveFlag = aCassette.isDispatchReserved();
                            Validations.check(dispatchReserveFlag, retCodeConfig.getAlreadyDispatchReservedCassette());

                        }
                        /*--------------------------------------*/
                        /*   Check Cassette's Transfer Status   */
                        /*--------------------------------------*/

                        /*-----------------------*/
                        /*   Get TransferState   */
                        /*-----------------------*/
                        String transferState = aCassette.getTransportState();
                        /*------------------------------*/
                        /*   Get TransferReserveState   */
                        /*------------------------------*/
                        Boolean transferReserved = aCassette.isReserved();
                        /*===== for OpeStart =====*/
                        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            Validations.check(!CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                        }
                        /*===== for StartReservation =====*/
                        else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationMode = portMethod.portResourceCurrentOperationModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());

                            Infos.EqpPortStatus orgEqpPortStatus = new Infos.EqpPortStatus();
                            CimMachine aOrgMachine = null;
                            if (BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                /*-------------------------------*/
                                /*   Get Originator Eqp's Info   */
                                /*-------------------------------*/

                                /*--------------------------------*/
                                /*   Get Originator EquipmentID   */
                                /*--------------------------------*/
                                Machine aMachine = aCassette.currentAssignedMachine();
                                if (aMachine != null) {
                                    Boolean isStorageBool = aMachine.isStorageMachine();
                                    if (!isStorageBool) {
                                        aOrgMachine = (CimMachine) aMachine;
                                    }
                                }
                                Validations.check(null == aOrgMachine, new OmCode(retCodeConfig.getNotFoundEqp(), strInParam.getEquipmentId().getValue()));

                                ObjectIdentifier orgEquipmentID = new ObjectIdentifier(aOrgMachine.getIdentifier(), aOrgMachine.getPrimaryKey());
                                /*---------------------------------*/
                                /*   Get Cassette Info in OrgEqp   */
                                /*---------------------------------*/
                                Infos.EqpPortInfo equipmentPortInfo = null;
                                String equipmentCategory = aOrgMachine.getCategory();
                                if (BizConstant.equalsIgnoreCase(equipmentCategory, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)) {
                                    equipmentPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, orgEquipmentID);
                                } else {
                                    equipmentPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, orgEquipmentID);
                                }
                                Boolean bFound = false;
                                int lenEqpPort = CimArrayUtils.getSize(equipmentPortInfo.getEqpPortStatuses());
                                for (int portCnt = 0; portCnt < lenEqpPort; portCnt++) {
                                    if (ObjectIdentifier.equalsWithValue(strInParam.getStartDurables().get(durableCnt).getDurableId(), equipmentPortInfo.getEqpPortStatuses().get(portCnt).getLoadedCassetteID())) {
                                        orgEqpPortStatus = equipmentPortInfo.getEqpPortStatuses().get(portCnt);
                                        bFound = true;
                                        break;
                                    }
                                }
                                Validations.check(!bFound, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                            }
                            if (BizConstant.equalsIgnoreCase(portResourceCurrentOperationMode.getOperationMode().getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_MANUAL)) {
                                /*-------------------------------------------------------------------------*/
                                /*   When TransferStatus is EI, AccessMode makes it an error with Manual   */
                                /*-------------------------------------------------------------------------*/
                                if (BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                    /*---------------------------------------------------------------------------*/
                                    /*   Permit Carrier which a person can deliver in StartLotReserve.           */
                                    /*   As for the condition, OperationMode is "***-1" and XferState is "EI".   */
                                    /*---------------------------------------------------------------------------*/
                                    if (!BizConstant.equalsIgnoreCase(orgEqpPortStatus.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_MANUAL)
                                            || ObjectIdentifier.isEmptyWithValue(orgEqpPortStatus.getLoadedCassetteID())) {
                                        Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                    }
                                }
                            } else {
                                Boolean bReRouteFlg = false;
                                String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                                if (CimStringUtils.equals(VALUE_ONE,reRouteXferFlag)
                                      && (ObjectIdentifier.equalsWithValue(BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_3, portResourceCurrentOperationMode.getOperationMode().getOperationMode()))
                                      && (CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_STATIONIN) ||
                                          CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_BAYIN) ||
                                          CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_MANUALIN) ||
                                          CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_BAYOUT))
                                      && CimBooleanUtils.isFalse(transferReserved)){
                                    log.info("operationMode is Auto-3");
                                    log.info("transferState = [SI], [BI], [MI], [BO] and transferReserved is FALSE");
                                }else if ((CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_STATIONIN) ||
                                        CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_BAYIN) ||
                                        CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_MANUALIN))
                                        && CimBooleanUtils.isFalse(transferReserved)){
                                    log.info("transferState = [SI], [BI], [MI] and transferReserved is FALSE");
                                }else if ((BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_STATIONOUT)
                                        || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT))
                                        && !transferReserved) {
                                    //-------------------------------------
                                    //  Check transfer job existence
                                    //-------------------------------------

                                    Infos.CarrierJobResult cassetteTransferJobRecordGetDROutRetCode = null;
                                    try {
                                        cassetteTransferJobRecordGetDROutRetCode = cassetteMethod.cassetteTransferJobRecordGetDR(objCommon, startDurable.getDurableId());
                                    } catch (ServiceException e) {
                                        if (Validations.isEquals(retCodeConfig.getCarrierNotTransfering(), e.getCode())) {
                                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                        }
                                        throw e;
                                    }
                                    Outputs.ObjStockerTypeGetDROut stockerTypeGetResult = null;
                                    try {
                                        stockerTypeGetResult = stockerMethod.stockerTypeGet(objCommon, cassetteTransferJobRecordGetDROutRetCode.getToMachine());
                                    } catch (ServiceException e) {
                                        if (Validations.isEquals(retCodeConfig.getCarrierNotTransfering(), e.getCode())) {
                                            Validations.check(true, retCodeConfig.getInterfabInvalidXferstate());
                                        } else {
                                            throw e;
                                        }
                                    }

                                } else if (BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) && CimBooleanUtils.isFalse(transferReserved)) {
                                    /*-----------------------------------------------------------------------------------------------*/
                                    /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                                    /*                                                                                               */
                                    /*   -----------------------------                                                               */
                                    /*   |         FromEQP           |                                                               */
                                    /*   ----------------------------|                                                               */
                                    /*   | OperationMode : Offline-2 |                                                               */
                                    /*   | XferState     : EI        |                                                               */
                                    /*   -----------------------------                                                               */
                                    /*-----------------------------------------------------------------------------------------------*/
                                    if (BizConstant.equalsIgnoreCase(orgEqpPortStatus.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                            && CimStringUtils.equals(orgEqpPortStatus.getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                                        Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                    }
                                    /*-----------------------------------------------------------------------------------------------*/
                                    /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                                    /*                                                                                               */
                                    /*   ToEQP's OperationMode : ***-2                                                               */
                                    /*-----------------------------------------------------------------------------------------------*/
                                    if (BizConstant.equalsIgnoreCase(portResourceCurrentOperationMode.getOperationMode().getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                            && BizConstant.equalsIgnoreCase(portResourceCurrentOperationMode.getOperationMode().getDispatchMode(), BizConstant.SP_EQP_DISPATCHMODE_AUTO)) {
                                        log.info("accessMode == SP_Eqp_AccessMode_Auto && dispatchMode == SP_Eqp_DispatchMode_Auto");
                                    } else {
                                        Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                    }
                                    /*---------------------------------------------------------*/
                                    /*   Check orgEqp's EqpToEqpTransfer Flag is TRUE or Not   */
                                    /*---------------------------------------------------------*/
                                    Boolean bEqpToEqpXFerFlag = aOrgMachine.isEqpToEqpTransferFlagOn();
                                    Validations.check(!bEqpToEqpXFerFlag, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                } else {
                                    Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                }
                            }
                        }
                        if (StandardProperties.OM_DRBL_CHK_FOR_EMPTY_CARRIER.getIntValue() == 0) {
                            /*--------------------------------*/
                            /*   Check Cassette is Empty      */
                            /*--------------------------------*/
                            Boolean bEmpty = aCassette.isEmpty();
                            Validations.check(!bEmpty, new OmCode(retCodeConfig.getCastNotEmpty(), startDurable.getDurableId().getValue()));
                        }
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*-----------------------------------------------------------------------*/
                            /*   Check Cassette's DurableControlJobID vs Eqp's DurableControlJobID   */
                            /*-----------------------------------------------------------------------*/
                            CimDurableControlJob aDurableControlJob = aCassette.getDurableControlJob();
                            Validations.check(null == aDurableControlJob, retCodeConfig.getDurableControlJobBlank());
                            ObjectIdentifier durableCJID = new ObjectIdentifier(aDurableControlJob.getIdentifier(), aDurableControlJob.getPrimaryKey());
                            List<ObjectIdentifier> reservedDurableControlJobIDs = equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, strInParam.getEquipmentId());
                            Boolean durableCJFound = false;
                            for (ObjectIdentifier reservedDurableControlJobID : reservedDurableControlJobIDs) {
                                if (ObjectIdentifier.equalsWithValue(durableCJID, reservedDurableControlJobID)) {
                                    durableCJFound = true;
                                    break;
                                }
                            }
                            Validations.check(!durableCJFound, retCodeConfig.getDurableEQPDrbCtrljobUnmatch(), startDurable.getDurableId().getValue());
                        }
                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                        aMachineList = aReticlePod.getQueuedMachines();
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*-------------------------------------------*/
                            /*   1. Check ReticleDispatchJob existence   */
                            /*-------------------------------------------*/
                            List<Infos.ReticleDispatchJob> reticleDispatchJobCheckExistenceResult = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, new ObjectIdentifier(), startDurable.getDurableId(), new ObjectIdentifier());

                        }
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aReticlePod.getDurableControlJob();
                        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());
                        }
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*-------------------------------------------------------------------------*/
                            /*   Check ReticlePod's DurableControlJobID vs Eqp's DurableControlJobID   */
                            /*-------------------------------------------------------------------------*/
                            Validations.check(aDurableControlJob == null, retCodeConfig.getDurableControlJobBlank());

                            ObjectIdentifier durableCJID = new ObjectIdentifier(aDurableControlJob.getIdentifier(), aDurableControlJob.getPrimaryKey());
                            List<ObjectIdentifier> reservedDurableControlJobIDs = equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, strInParam.getEquipmentId());
                            Boolean durableCJFound = false;
                            for (int reservedDurableCJCnt = 0; reservedDurableCJCnt < reservedDurableControlJobIDs.size(); reservedDurableCJCnt++) {
                                if (ObjectIdentifier.equalsWithValue(durableCJID, reservedDurableControlJobIDs.get(reservedDurableCJCnt))) {
                                    durableCJFound = true;
                                    break;
                                }
                            }
                            Validations.check(!durableCJFound, new OmCode(retCodeConfig.getDurableEQPDrbCtrljobUnmatch(), startDurable.getDurableId().getValue()));
                        }
                        /*----------------------------------------*/
                        /*   Check ReticlePod's Transfer Status   */
                        /*----------------------------------------*/
                        String transferStatus = aReticlePod.getTransferStatus();
                        /*===== for OpeStart =====*/
                        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            if (!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                Validations.check(true, new OmCode(retCodeConfig.getInvalidReticlepodXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                            }
                        }
                        /*===== for StartReservation =====*/
                        else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            if (BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetResult = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, strInParam.getEquipmentId());

                                // ReticlePod is loaded by specified port
                                List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = equipmentReticlePodPortInfoGetResult.getReticlePodPortInfoList();
                                int rppLen = CimArrayUtils.getSize(reticlePodPortInfoList);
                                for (int rtclPodPortCnt = 0; rtclPodPortCnt < rppLen; rtclPodPortCnt++) {
                                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfoList.get(rtclPodPortCnt).getReticlePodPortID(), startDurable.getStartDurablePort().getLoadPortID())) {
                                        if (!ObjectIdentifier.equalsWithValue(startDurable.getDurableId(), reticlePodPortInfoList.get(rtclPodPortCnt).getLoadedReticlePodID())) {
                                            Validations.check(true, new OmCode(retCodeConfig.getReticlepodLoadedReticlepod(), startDurable.getDurableId().getValue(),
                                                    reticlePodPortInfoList.get(rtclPodPortCnt).getReticlePodPortID().getValue(), strInParam.getEquipmentId().getValue(),
                                                    reticlePodPortInfoList.get(rtclPodPortCnt).getReticlePodPortID().getValue()));
                                        }
                                    }
                                }
                            }
                        }
                        if (StandardProperties.OM_DRBL_CHK_FOR_EMPTY_POD.getIntValue() == 0) {
                            /*----------------------------------*/
                            /*   Check reticlePod is Empty      */
                            /*----------------------------------*/
                            boolean bEmpty = aReticlePod.isEmpty();
                            Validations.check(!bEmpty, new OmCode(retCodeConfig.getReticlepodNotEmpty(), startDurable.getDurableId().getValue()));
                        }

                    } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
                        aMachineList = aReticle.getQueuedMachines();
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*-------------------------------------------*/
                            /*   1. Check ReticleDispatchJob existence   */
                            /*-------------------------------------------*/
                            List<Infos.ReticleDispatchJob> reticleDispatchJobCheckExistenceDRResult = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, startDurable.getDurableId(), new ObjectIdentifier(), new ObjectIdentifier());
                        }
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aReticle.getDurableControlJob();
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            Validations.check(null != aDurableControlJob, retCodeConfig.getDurableControlJobFilled());
                        }
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*----------------------------------------------------------------------*/
                            /*   Check Reticle's DurableControlJobID vs Eqp's DurableControlJobID   */
                            /*----------------------------------------------------------------------*/
                            Validations.check(aDurableControlJob == null, retCodeConfig.getDurableControlJobBlank());

                            ObjectIdentifier durableCJID = new ObjectIdentifier(aDurableControlJob.getIdentifier(), aDurableControlJob.getPrimaryKey());
                            List<ObjectIdentifier> reservedDurableControlJobIDs = equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, strInParam.getEquipmentId());
                            Boolean durableCJFound = false;
                            for (int reservedDurableCJCnt = 0; reservedDurableCJCnt < reservedDurableControlJobIDs.size(); reservedDurableCJCnt++) {
                                if (ObjectIdentifier.equalsWithValue(durableCJID, reservedDurableControlJobIDs.get(reservedDurableCJCnt))) {
                                    durableCJFound = true;
                                    break;
                                }
                            }
                            Validations.check(!durableCJFound, new OmCode(retCodeConfig.getDurableEQPDrbCtrljobUnmatch(), startDurable.getDurableId().getValue()));

                        }
                        /*----------------------------------------*/
                        /*   Check Reticle's Transfer Status      */
                        /*----------------------------------------*/
                        String transferStatus = aReticle.getTransportState();
                        /*===== for OpeStart =====*/
                        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            Validations.check(!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidReticleXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                        }
                        /*===== for StartReservation =====*/
                        else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            if (BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                List<Infos.StoredReticle> storedReticleList = equipmentMethod.equipmentStoredReticleGetDR(objCommon, strInParam.getEquipmentId());

                                // Reticle is in the specified equipment
                                Boolean bReticleFoundInEqp = false;
                                int srLen = CimArrayUtils.getSize(storedReticleList);
                                for (int srCnt = 0; srCnt < srLen; srCnt++) {
                                    if (ObjectIdentifier.equalsWithValue(startDurable.getDurableId(), storedReticleList.get(srCnt).getReticleID())) {
                                        bReticleFoundInEqp = true;
                                        break;
                                    }
                                }
                                Validations.check(!bReticleFoundInEqp, new OmCode(retCodeConfig.getReticleNotInTheEqp(), startDurable.getDurableId().getValue()));
                            }
                        }
                    }
                    String durableInventoryStateGetResult = this.durableInventoryStateGet(objCommon, durableCategory, startDurable.getDurableId());
                    Validations.check(BizConstant.equalsIgnoreCase(durableInventoryStateGetResult, BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK), new OmCode(retCodeConfig.getInvalidDurableInventoryStat(), startDurable.getDurableId().getValue(), durableInventoryStateGetResult));

                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            String objDurableHoldStateGetOutRetCode = this.durableHoldStateGet(objCommon, durableCategory, startDurable.getDurableId());

                            Validations.check(!BizConstant.equalsIgnoreCase(objDurableHoldStateGetOutRetCode, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD), new OmCode(retCodeConfig.getInvalidDurableHoldStat(), startDurable.getDurableId().getValue(), objDurableHoldStateGetOutRetCode));

                            String objDurableProcessStateGetOutRetCode = this.durableProcessStateGet(objCommon, durableCategory, startDurable.getDurableId());

                            Validations.check(!BizConstant.equalsIgnoreCase(objDurableProcessStateGetOutRetCode, BizConstant.SP_DURABLE_PROCSTATE_WAITING), new OmCode(retCodeConfig.getInvalidDurableProcStat(), startDurable.getDurableId().getValue(), objDurableHoldStateGetOutRetCode));

                            Boolean matchFlag = false;
                            int machineLen = CimArrayUtils.getSize(aMachineList);
                            for (int machineSeq = 0; machineSeq < machineLen; machineSeq++) {
                                ObjectIdentifier qEquipmentID = new ObjectIdentifier(aMachineList.get(machineSeq).getIdentifier(), aMachineList.get(machineSeq).getPrimaryKey());
                                if (ObjectIdentifier.equalsWithValue(qEquipmentID, strInParam.getEquipmentId())) {
                                    matchFlag = true;
                                    break;
                                }

                            }
                            Validations.check(!matchFlag, retCodeConfig.getNotCorrectEqpForOpestartForDurable());

                            try {
                                this.durableCheckEndBankIn(objCommon, durableCategory, startDurable.getDurableId());
                            } catch (ServiceException ex) {
                                if (Validations.isEquals(ex.getCode(), retCodeConfig.getBankinOperation())) {
                                    throw ex;
                                }
                            }

                            //-----------------------//
                            //   Check Route/OpeNo   //
                            //-----------------------//
                            if (!ObjectIdentifier.isEmptyWithValue(startDurable.getStartOperationInfo().getProcessFlowID())
                                    && !CimObjectUtils.isEmpty(startDurable.getStartOperationInfo().getOperationNumber())) {
                                Outputs.ObjDurableCurrentOperationInfoGetOut objDurableCurrentOperationInfoGetOutRetCode = this.durableCurrentOperationInfoGet(objCommon, durableCategory, startDurable.getDurableId());

                                if (ObjectIdentifier.equalsWithValue(startDurable.getStartOperationInfo().getProcessFlowID(), objDurableCurrentOperationInfoGetOutRetCode.getRouteID())
                                        && CimStringUtils.equals(startDurable.getStartOperationInfo().getOperationNumber(), objDurableCurrentOperationInfoGetOutRetCode.getOperationNumber())) {
                                    log.info("{} Route/Operation check OK. Go ahead...", startDurable.getStartOperationInfo().getProcessFlowID());
                                } else {
                                    Validations.check(true, retCodeConfig.getInvalidInputParam());
                                }
                            } else {
                                log.info("Route/Operation check skipped. Go ahead...");
                            }
                        }
                    }
                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTARTCANCEL)) {
                    if (BizConstant.equalsIgnoreCase(strInParam.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
                        Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeGetResult = portMethod.portResourceCurrentOperationModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());

                        if (BizConstant.equalsIgnoreCase(portResourceCurrentOperationModeGetResult.getOperationMode().getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                            String transferState = aCassette.getTransportState();
                            Validations.check(!BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                        }
                    } else if (BizConstant.equalsIgnoreCase(strInParam.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
                        Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut objReticlePodPortResourceCurrentAccessModeGetOutRetCode = reticleMethod.reticlePodPortResourceCurrentAccessModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());
                        if (BizConstant.equalsIgnoreCase(objReticlePodPortResourceCurrentAccessModeGetOutRetCode.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                            String transferStatus = aReticlePod.getTransferStatus();
                            Validations.check(!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidReticlepodXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                        }
                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                        String transferStatus = aReticle.getTransportState();
                        Validations.check(!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidReticleXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                    }

                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            String durableHoldStateGetResult = this.durableHoldStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!BizConstant.equalsIgnoreCase(durableHoldStateGetResult, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD), new OmCode(retCodeConfig.getInvalidDurableHoldStat(), startDurable.getDurableId().getValue(), durableHoldStateGetResult));

                            String durableProcessStateGetResult = this.durableProcessStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!BizConstant.equalsIgnoreCase(durableProcessStateGetResult, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING), new OmCode(retCodeConfig.getInvalidDurableProcStat(), startDurable.getDurableId().getValue(), durableProcessStateGetResult));

                        }
                    }

                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPERATIONCOMP)) {
                    String transferStatus = null;
                    String accessMode = null;
                    String onlineMode = null;
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeGetResult = portMethod.portResourceCurrentOperationModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());

                        accessMode = portResourceCurrentOperationModeGetResult.getOperationMode().getAccessMode();
                        onlineMode = portResourceCurrentOperationModeGetResult.getOperationMode().getOnlineMode();
                        transferStatus = aCassette.getTransportState();
                    } else {
                        /*-----------------------------------------*/
                        /*   Get Equipment's Operation Mode Info   */
                        /*-----------------------------------------*/
                        String equipmentOnlineModeGetResult = equipmentMethod.equipmentOnlineModeGet(objCommon, strInParam.getEquipmentId());

                        onlineMode = equipmentOnlineModeGetResult;
                        if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                            Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut objReticlePodPortResourceCurrentAccessModeGetOutRetCode = reticleMethod.reticlePodPortResourceCurrentAccessModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());

                            accessMode = objReticlePodPortResourceCurrentAccessModeGetOutRetCode.getAccessMode();
                            transferStatus = aReticlePod.getTransferStatus();
                        } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                            transferStatus = aReticle.getTransportState();
                        }
                    }
                    if (BizConstant.equalsIgnoreCase(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                            || BizConstant.equalsIgnoreCase(accessMode, BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                        Validations.check(!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), retCodeConfig.getInvalidTransferState());
                    }
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        //-----------------------------------------------------------
                        // Check cassette interFabXferState
                        //-----------------------------------------------------------
                        String cassetteInterFabTransferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommon, startDurable.getDurableId());
                        Validations.check(CimStringUtils.equals(cassetteInterFabTransferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                                retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(),
                                startDurable.getDurableId().getValue(),
                                cassetteInterFabTransferStateGetResult);
                    }
                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            String durableHoldStateGetResult = this.durableHoldStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!BizConstant.equalsIgnoreCase(durableHoldStateGetResult, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD),
                                    new OmCode(retCodeConfig.getInvalidDurableHoldStat(), startDurable.getDurableId().getValue(), durableHoldStateGetResult));

                            String durableProcessStateGetResult = this.durableProcessStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!BizConstant.equalsIgnoreCase(durableProcessStateGetResult, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING),
                                    new OmCode(retCodeConfig.getInvalidDurableProcStat(), startDurable.getDurableId().getValue(), durableProcessStateGetResult));
                        }
                    }

                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL)) {
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        /*----------------------------------------------*/
                        /*   Get and Check Cassette's Dispatch Status   */
                        /*----------------------------------------------*/
                        Boolean dispatchReserveFlag = aCassette.isDispatchReserved();
                        Validations.check(!dispatchReserveFlag, retCodeConfig.getNotDispatchReservedCassette());
                    }
                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            String durableProcessStateGetResult = this.durableProcessStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(BizConstant.equalsIgnoreCase(durableProcessStateGetResult, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING),
                                    new OmCode(retCodeConfig.getInvalidDurableProcStat(), startDurable.getDurableId().getValue(), durableProcessStateGetResult));
                        }

                    }

                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_LOADING)) {
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        /*-------------------------------*/
                        /*   Check SorterJob existence   */
                        /*-------------------------------*/
                        List<ObjectIdentifier> dummyIDs = new ArrayList<>();
                        Infos.EquipmentLoadPortAttribute equipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
                        List<Infos.CassetteLoadPort> cassetteLoadPortList = new ArrayList<>();
                        equipmentLoadPortAttribute.setCassetteLoadPortList(cassetteLoadPortList);
                        Infos.CassetteLoadPort cassetteLoadPort = new Infos.CassetteLoadPort();
                        cassetteLoadPortList.add(cassetteLoadPort);
                        cassetteLoadPort.setPortID(null == startDurable.getStartDurablePort() ? null : startDurable.getStartDurablePort().getLoadPortID());
                        cassetteLoadPort.setCassetteID(startDurable.getDurableId());
                        equipmentLoadPortAttribute.setEquipmentID(strInParam.getEquipmentId());

                        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
                        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(equipmentLoadPortAttribute);
                        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyIDs);
                        objWaferSorterJobCheckForOperation.setLotIDList(dummyIDs);
                        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_LOADINGLOT);
                        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
                        /*--------------------------------------*/
                        /*   Check Cassette's Transfer Status   */
                        /*--------------------------------------*/

                        /*-----------------------*/
                        /*   Get TransferState   */
                        /*-----------------------*/
                        String transferState = aCassette.getTransportState();
                        if (BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_STATIONOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_MANUALOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_SHELFOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_ABNORMALOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_UNDEFINED_STATE)) {
                            log.info("transferState == {}", transferState);
                        } else {
                            Validations.check(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                        }
                        //-----------------------------------------------------------
                        // Check cassette interFabXferState
                        //-----------------------------------------------------------
                        String cassetteInterFabTransferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommon, startDurable.getDurableId());
                        Validations.check(CimStringUtils.equals(cassetteInterFabTransferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                                new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), startDurable.getDurableId().getValue(), cassetteInterFabTransferStateGetResult));
                        if (StandardProperties.OM_DRBL_CHK_FOR_EMPTY_CARRIER.getIntValue() == 0) {

                            /*--------------------------------*/
                            /*   Check Cassette is Empty      */
                            /*--------------------------------*/
                            Boolean bEmpty = aCassette.isEmpty();
                            Validations.check(!bEmpty, new OmCode(retCodeConfig.getCastNotEmpty(), startDurable.getDurableId().getValue()));
                        }
                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                        /*----------------------------------------*/
                        /*   Check ReticlePod's Transfer Status   */
                        /*----------------------------------------*/
                        String transferStatus = aReticlePod.getTransferStatus();
                        if (BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_UNDEFINED_STATE)) {
                            log.info("transferStatus == {}", transferStatus);
                        } else {
                            Validations.check(true, new OmCode(retCodeConfig.getInvalidReticlepodXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                        }
                        if (StandardProperties.OM_DRBL_CHK_FOR_EMPTY_POD.getIntValue() == 0) {
                            /*----------------------------------*/
                            /*   Check reticlePod is Empty      */
                            /*----------------------------------*/
                            boolean bEmpty = aReticlePod.isEmpty();
                            Validations.check(!bEmpty, new OmCode(retCodeConfig.getReticlepodNotEmpty(), startDurable.getDurableId().getValue()));
                        }
                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                        /*----------------------------------------*/
                        /*   Check Reticle's Transfer Status      */
                        /*----------------------------------------*/
                        String transferStatus = aReticle.getTransportState();
                        if (BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_UNDEFINED_STATE)) {
                            log.info("transferStatus == {}", transferStatus);
                        } else {
                            Validations.check(true, new OmCode(retCodeConfig.getInvalidReticleXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                        }
                    }
                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_PFXDELETE)) {
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aCassette.getDurableControlJob();
                        Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());
                    } else if (BizConstant.equalsIgnoreCase(strInParam.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aReticlePod.getDurableControlJob();
                        Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());

                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aReticle.getDurableControlJob();
                        Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());

                    }
                    //---------------------------------------
                    //  Get InPostProcessFlag of Cassette
                    //---------------------------------------
                    Boolean durableInPostProcessFlagGetResult = this.durableInPostProcessFlagGet(objCommon, durableCategory, startDurable.getDurableId());
                    Validations.check(durableInPostProcessFlagGetResult, new OmCode(retCodeConfig.getDurableInPostProcess(), startDurable.getDurableId().getValue()));

                }
                durableCnt++;
            }
        }
        /*-------------------------------------------------------*/
        /*   Check Upper/Lower Limit for RecipeParameterChange   */
        /*-------------------------------------------------------*/
        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)
                || BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, strInParam.getEquipmentId());
            List<RecipeDTO.RecipeParameter> aRecipeParameters = aMachine.getRecipeParameters();
            if (!CimObjectUtils.isEmpty(aRecipeParameters)) {
                for (RecipeDTO.RecipeParameter recipeParameter : aRecipeParameters) {
                    if (BizConstant.equalsIgnoreCase(recipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_STRING)) {
                        log.info("########## dataType is SP_DCDef_Val_String -----> continue !!");
                        continue;
                    }
                    Boolean bRcpParamFound = false;
                    int startRcpParamCnt = 0;
                    List<Infos.StartRecipeParameter> startRecipeParameters = durableStartRecipe.getStartRecipeParameterS();
                    for (startRcpParamCnt = 0; startRcpParamCnt < startRecipeParameters.size(); startRcpParamCnt++) {
                        if (CimStringUtils.equals(startRecipeParameters.get(startRcpParamCnt).getParameterName(), recipeParameter.getParameterName())) {
                            bRcpParamFound = true;
                            break;
                        }
                    }
                    if (bRcpParamFound) {
                        if (recipeParameter.getUseCurrentValueFlag()) {
                            Validations.check(!CimObjectUtils.isEmpty(startRecipeParameters.get(startRcpParamCnt).getParameterValue()), new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), recipeParameter.getParameterName()));
                        } else {
                            if (BizConstant.equalsIgnoreCase(recipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                String parameterValueStr = startRecipeParameters.get(startRcpParamCnt).getParameterValue();
                                Long lowerLimit = Long.parseLong(recipeParameter.getLowerLimit());
                                Long upperLimit = Long.parseLong(recipeParameter.getUpperLimit());
                                Long parameterValue = Long.parseLong(parameterValueStr);
                                Validations.check((parameterValue < lowerLimit || parameterValue > upperLimit),
                                        new OmCode(retCodeConfig.getInvalidParameterValueRange(), recipeParameter.getParameterName(), recipeParameter.getLowerLimit(), recipeParameter.getUpperLimit()));
                            } else if (BizConstant.equalsIgnoreCase(recipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                double parameterValue = Double.parseDouble(startRecipeParameters.get(startRcpParamCnt).getParameterValue());
                                double lowerLimit = Double.parseDouble(recipeParameter.getLowerLimit());
                                double upperLimit = Double.parseDouble(recipeParameter.getUpperLimit());
                                Validations.check(parameterValue < lowerLimit || parameterValue > upperLimit, new OmCode(retCodeConfig.getInvalidParameterValueRange(), recipeParameter.getParameterName(), recipeParameter.getLowerLimit(), recipeParameter.getUpperLimit()));
                            }
                        }
                    }
                }
            }
        }
        /*---------------------------------------------------------------------*/
        /*   Get and Check Entity Inhibition for OpeStart / StartReservation   */
        /*---------------------------------------------------------------------*/
        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
            List<ObjectIdentifier> durableIDs = startDurableList.stream().map(startDurable -> startDurable.getDurableId()).collect(Collectors.toList());
            Inputs.ObjEquipmentCheckInhibitForDurableWithMachineRecipeIn objEquipmentCheckInhibitForDurableWithMachineRecipeIn = new Inputs.ObjEquipmentCheckInhibitForDurableWithMachineRecipeIn();
            objEquipmentCheckInhibitForDurableWithMachineRecipeIn.setEquipmentID(strInParam.getEquipmentId());
            objEquipmentCheckInhibitForDurableWithMachineRecipeIn.setDurableIDs(durableIDs);
            objEquipmentCheckInhibitForDurableWithMachineRecipeIn.setDurableCategory(strInParam.getDurableCategory());
            objEquipmentCheckInhibitForDurableWithMachineRecipeIn.setDurableStartRecipe(durableStartRecipe);
            equipmentMethod.equipmentCheckInhibitForDurableWithMachineRecipe(objCommon, objEquipmentCheckInhibitForDurableWithMachineRecipeIn);

        }
    }

    @Override
    public void durableCheckConditionForOperationForInternalBuffer(Infos.ObjCommon objCommon, Inputs.ObjDurableCheckConditionForOperationIn strInParam) {
        List<CimMachine> aMachineList = null;
        Boolean isBankInRequired = false;
        String durableCategory = strInParam.getDurableCategory();
        if (!BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)
                && !BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)
                && !BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
            log.info("{}", "Invalid durable category", durableCategory);
            Validations.check(true, new OmCode(retCodeConfig.getInvalidDurableCategory(), durableCategory));
        }
        int sequenceCondition = StandardProperties.OM_CARRIER_LOAD_SEQ_CHK.getIntValue();
        //-------------------------//
        //   Check LogicalRecipe   //
        //-------------------------//
        List<Infos.StartDurable> startDurableList = strInParam.getStartDurables();
        Infos.DurableStartRecipe durableStartRecipe = strInParam.getDurableStartRecipe();
        if (null != durableStartRecipe && !ObjectIdentifier.isEmptyWithValue(durableStartRecipe.getLogicalRecipeId())
                && (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)
                || BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART))) {
            if (!CimObjectUtils.isEmpty(startDurableList)) {
                String invalidDurableID = null;
                int invalidCnt = 0;
                for (Infos.StartDurable startDurable : startDurableList) {
                    Outputs.ObjDurableRecipeGetOut durableRecipeGetOut = this.durableRecipeGet(objCommon, durableCategory, startDurable.getDurableId(), strInParam.getEquipmentId());

                    if (!ObjectIdentifier.equalsWithValue(durableRecipeGetOut.getLogicalRecipeID(), durableStartRecipe.getLogicalRecipeId())) {
                        invalidCnt++;
                        if (CimStringUtils.isEmpty(invalidDurableID)) {
                            invalidDurableID = startDurable.getDurableId().getValue();
                        }
                    }
                }
                if (invalidCnt > 0) {
                    if (invalidCnt != startDurableList.size()) {
                        Validations.check(true, new OmCode(retCodeConfig.getNotSameLogicalRecipeForDurable(), invalidDurableID));
                    } else {
                        Validations.check(true, new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "Logical Recipe is invalid."));
                    }
                }
            }
        }
        if (!CimObjectUtils.isEmpty(startDurableList)) {
            int durableCnt = 0;
            for (Infos.StartDurable startDurable : startDurableList) {
                CimCassette aCassette = null;
                CimDurableProcessFlowContext aDurablePFX = null;
                CimReticlePod aReticlePod = null;
                CimProcessDurable aReticle = null;
                if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                    aCassette = baseCoreFactory.getBO(CimCassette.class, startDurable.getDurableId());

                    aDurablePFX = aCassette.getDurableProcessFlowContext();
                } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                    aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, startDurable.getDurableId());
                    aDurablePFX = aReticlePod.getDurableProcessFlowContext();
                } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                    aReticle = baseCoreFactory.getBO(CimProcessDurable.class, startDurable.getDurableId());
                    aDurablePFX = aReticle.getDurableProcessFlowContext();
                }
                if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_PFXCREATE)) {
                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            log.info("##### durable is on route");
                            //---------------------------------------
                            // Check Durable Inventory State
                            //---------------------------------------
                            String durableInventoryStateGetResult = this.durableInventoryStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!CimStringUtils.equals(durableInventoryStateGetResult, BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK), new OmCode(retCodeConfig.getInvalidDurableInventoryStat(), startDurable.getDurableId().getValue()));
                        }
                    }
                } else {
                    Validations.check(aDurablePFX == null, new OmCode(retCodeConfig.getNotFoundPfx(), ""));
                }
                if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)
                        || CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                    //---------------------------------------
                    //  Get InPostProcessFlag of Cassette
                    //---------------------------------------
                    Boolean durableInPostProcessFlagGet = this.durableInPostProcessFlagGet(objCommon, durableCategory, startDurable.getDurableId());
                    Validations.check(durableInPostProcessFlagGet, new OmCode(retCodeConfig.getDurableInPostProcess(), startDurable.getDurableId().getValue()));

                    //------------------------------------
                    //   Check equipment availability
                    //------------------------------------
                    equipmentMethod.equipmentCheckAvailForDurable(objCommon, strInParam.getEquipmentId());

                    if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        //-----------------------------------------------------------
                        // Check cassette interFabXferState
                        //-----------------------------------------------------------
                        String cassetteInterFabXferStateGet = cassetteMethod.cassetteInterFabXferStateGet(objCommon, startDurable.getDurableId());
                        Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, cassetteInterFabXferStateGet)
                                , new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), startDurable.getDurableId().getValue(), cassetteInterFabXferStateGet));

                        aMachineList = aCassette.getQueuedMachines();
                        /*-------------------------------*/
                        /*   Check SorterJob existence   */
                        /*-------------------------------*/
                        Infos.EquipmentLoadPortAttribute equipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
                        List<Infos.CassetteLoadPort> cassetteLoadPortList = new ArrayList<>();
                        equipmentLoadPortAttribute.setCassetteLoadPortList(cassetteLoadPortList);
                        Infos.CassetteLoadPort cassetteLoadPort = new Infos.CassetteLoadPort();
                        cassetteLoadPortList.add(cassetteLoadPort);
                        cassetteLoadPort.setPortID(startDurable.getStartDurablePort().getLoadPortID());
                        cassetteLoadPort.setCassetteID(startDurable.getDurableId());
                        equipmentLoadPortAttribute.setEquipmentID(strInParam.getEquipmentId());

                        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
                        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(equipmentLoadPortAttribute);
                        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
                        objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIDList);
                        cassetteIDList.add(startDurable.getDurableId());
                        objWaferSorterJobCheckForOperation.setLotIDList(new ArrayList<>());
                        objWaferSorterJobCheckForOperation.setOperation(strInParam.getOperation());
                        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            /*--------------------------------*/
                            /*   Get and Check ControlJobID   */
                            /*--------------------------------*/
                            CimControlJob aControlJob = aCassette.getControlJob();
                            Validations.check(aControlJob != null, retCodeConfig.getCassetteControlJobFilled());
                            /*---------------------------------------*/
                            /*   Get and Check DurableControlJobID   */
                            /*---------------------------------------*/
                            CimDurableControlJob aDurableControlJob = aCassette.getDurableControlJob();
                            Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());
                            /*----------------------------------------------*/
                            /*   Get and Check Cassette's Dispatch Status   */
                            /*----------------------------------------------*/
                            boolean dispatchReserveFlag = aCassette.isDispatchReserved();
                            Validations.check(dispatchReserveFlag, retCodeConfig.getAlreadyDispatchReservedCassette());

                        }
                        /*--------------------------------------*/
                        /*   Check Cassette's Transfer Status   */
                        /*--------------------------------------*/

                        /*-----------------------*/
                        /*   Get TransferState   */
                        /*-----------------------*/
                        String transferState = aCassette.getTransportState();
                        /*------------------------------*/
                        /*   Get TransferReserveState   */
                        /*------------------------------*/
                        Boolean transferReserved = aCassette.isReserved();
                        /*===== for OpeStart =====*/
                        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            Validations.check(!CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                        }
                        /*===== for StartReservation =====*/
                        else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationMode = portMethod.portResourceCurrentOperationModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());

                            Infos.EqpPortStatus orgEqpPortStatus = new Infos.EqpPortStatus();
                            CimMachine aOrgMachine = null;
                            if (BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                /*-------------------------------*/
                                /*   Get Originator Eqp's Info   */
                                /*-------------------------------*/

                                /*--------------------------------*/
                                /*   Get Originator EquipmentID   */
                                /*--------------------------------*/
                                Machine aMachine = aCassette.currentAssignedMachine();
                                if (aMachine != null) {
                                    Boolean isStorageBool = aMachine.isStorageMachine();
                                    if (!isStorageBool) {
                                        aOrgMachine = (CimMachine) aMachine;
                                    }
                                }
                                Validations.check(null == aOrgMachine, new OmCode(retCodeConfig.getNotFoundEqp(), strInParam.getEquipmentId().getValue()));

                                ObjectIdentifier orgEquipmentID = new ObjectIdentifier(aOrgMachine.getIdentifier(), aOrgMachine.getPrimaryKey());
                                /*---------------------------------*/
                                /*   Get Cassette Info in OrgEqp   */
                                /*---------------------------------*/
                                Infos.EqpPortInfo equipmentPortInfo = null;
                                String equipmentCategory = aOrgMachine.getCategory();
                                if (BizConstant.equalsIgnoreCase(equipmentCategory, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)) {
                                    equipmentPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, orgEquipmentID);
                                } else {
                                    equipmentPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, orgEquipmentID);
                                }
                                Boolean bFound = false;
                                int lenEqpPort = CimArrayUtils.getSize(equipmentPortInfo.getEqpPortStatuses());
                                for (int portCnt = 0; portCnt < lenEqpPort; portCnt++) {
                                    if (ObjectIdentifier.equalsWithValue(strInParam.getStartDurables().get(durableCnt).getDurableId(), equipmentPortInfo.getEqpPortStatuses().get(portCnt).getLoadedCassetteID())) {
                                        orgEqpPortStatus = equipmentPortInfo.getEqpPortStatuses().get(portCnt);
                                        bFound = true;
                                        break;
                                    }
                                }
                                Validations.check(!bFound, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                            }
                            if (BizConstant.equalsIgnoreCase(portResourceCurrentOperationMode.getOperationMode().getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_MANUAL)) {
                                /*-------------------------------------------------------------------------*/
                                /*   When TransferStatus is EI, AccessMode makes it an error with Manual   */
                                /*-------------------------------------------------------------------------*/
                                if (BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                    /*---------------------------------------------------------------------------*/
                                    /*   Permit Carrier which a person can deliver in StartLotReserve.           */
                                    /*   As for the condition, OperationMode is "***-1" and XferState is "EI".   */
                                    /*---------------------------------------------------------------------------*/
                                    if (!BizConstant.equalsIgnoreCase(orgEqpPortStatus.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_MANUAL)
                                            || ObjectIdentifier.isEmptyWithValue(orgEqpPortStatus.getLoadedCassetteID())) {
                                        Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                    }
                                }
                            } else {
                                Boolean bReRouteFlg = false;
                                String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                                if (CimStringUtils.equals(VALUE_ONE,reRouteXferFlag)
                                        && (ObjectIdentifier.equalsWithValue(BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_3, portResourceCurrentOperationMode.getOperationMode().getOperationMode()))
                                        && (CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_STATIONIN) ||
                                        CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_BAYIN) ||
                                        CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_MANUALIN) ||
                                        CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_BAYOUT))
                                        && CimBooleanUtils.isFalse(transferReserved)){
                                    log.info("operationMode is Auto-3");
                                    log.info("transferState = [SI], [BI], [MI], [BO] and transferReserved is FALSE");
                                }else if ((CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_STATIONIN) ||
                                        CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_BAYIN) ||
                                        CimStringUtils.equals(transferState,BizConstant.SP_TRANSSTATE_MANUALIN))
                                        && CimBooleanUtils.isFalse(transferReserved)) {
                                    log.info("transferState = [SI], [BI], [MI] and transferReserved is FALSE");
                                }else if ((BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_STATIONOUT)
                                        || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT))
                                        && !transferReserved) {
                                    //-------------------------------------
                                    //  Check transfer job existence
                                    //-------------------------------------

                                    Infos.CarrierJobResult cassetteTransferJobRecordGetDROutRetCode = null;
                                    try {
                                        cassetteTransferJobRecordGetDROutRetCode = cassetteMethod.cassetteTransferJobRecordGetDR(objCommon, startDurable.getDurableId());
                                    } catch (ServiceException e) {
                                        if (Validations.isEquals(retCodeConfig.getCarrierNotTransfering(), e.getCode())) {
                                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                        }
                                        throw e;
                                    }
                                    Outputs.ObjStockerTypeGetDROut stockerTypeGetResult = null;
                                    try {
                                        stockerTypeGetResult = stockerMethod.stockerTypeGet(objCommon, cassetteTransferJobRecordGetDROutRetCode.getToMachine());
                                    } catch (ServiceException e) {
                                        if (Validations.isEquals(retCodeConfig.getCarrierNotTransfering(), e.getCode())) {
                                            Validations.check(true, retCodeConfig.getInterfabInvalidXferstate());
                                        } else {
                                            throw e;
                                        }
                                    }

                                } else if (BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) && CimBooleanUtils.isFalse(transferReserved)) {
                                    /*-----------------------------------------------------------------------------------------------*/
                                    /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                                    /*                                                                                               */
                                    /*   -----------------------------                                                               */
                                    /*   |         FromEQP           |                                                               */
                                    /*   ----------------------------|                                                               */
                                    /*   | OperationMode : Offline-2 |                                                               */
                                    /*   | XferState     : EI        |                                                               */
                                    /*   -----------------------------                                                               */
                                    /*-----------------------------------------------------------------------------------------------*/
                                    if (BizConstant.equalsIgnoreCase(orgEqpPortStatus.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                            && CimStringUtils.equals(orgEqpPortStatus.getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                                        Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                    }
                                    /*-----------------------------------------------------------------------------------------------*/
                                    /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                                    /*                                                                                               */
                                    /*   ToEQP's OperationMode : ***-2                                                               */
                                    /*-----------------------------------------------------------------------------------------------*/
                                    if (BizConstant.equalsIgnoreCase(portResourceCurrentOperationMode.getOperationMode().getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                            && BizConstant.equalsIgnoreCase(portResourceCurrentOperationMode.getOperationMode().getDispatchMode(), BizConstant.SP_EQP_DISPATCHMODE_AUTO)) {
                                        log.info("accessMode == SP_Eqp_AccessMode_Auto && dispatchMode == SP_Eqp_DispatchMode_Auto");
                                    } else {
                                        Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                    }
                                    /*---------------------------------------------------------*/
                                    /*   Check orgEqp's EqpToEqpTransfer Flag is TRUE or Not   */
                                    /*---------------------------------------------------------*/
                                    Boolean bEqpToEqpXFerFlag = aOrgMachine.isEqpToEqpTransferFlagOn();
                                    Validations.check(!bEqpToEqpXFerFlag, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                } else {
                                    Validations.check(true, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                                }
                            }
                        }
                        if (StandardProperties.OM_DRBL_CHK_FOR_EMPTY_CARRIER.getIntValue() == 0) {
                            /*--------------------------------*/
                            /*   Check Cassette is Empty      */
                            /*--------------------------------*/
                            Boolean bEmpty = aCassette.isEmpty();
                            Validations.check(!bEmpty, new OmCode(retCodeConfig.getCastNotEmpty(), startDurable.getDurableId().getValue()));
                        }
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*-----------------------------------------------------------------------*/
                            /*   Check Cassette's DurableControlJobID vs Eqp's DurableControlJobID   */
                            /*-----------------------------------------------------------------------*/
                            CimDurableControlJob aDurableControlJob = aCassette.getDurableControlJob();
                            Validations.check(null == aDurableControlJob, retCodeConfig.getDurableControlJobBlank());
                            ObjectIdentifier durableCJID = new ObjectIdentifier(aDurableControlJob.getIdentifier(), aDurableControlJob.getPrimaryKey());
                            List<ObjectIdentifier> reservedDurableControlJobIDs = equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, strInParam.getEquipmentId());
                            Boolean durableCJFound = false;
                            for (ObjectIdentifier reservedDurableControlJobID : reservedDurableControlJobIDs) {
                                if (ObjectIdentifier.equalsWithValue(durableCJID, reservedDurableControlJobID)) {
                                    durableCJFound = true;
                                    break;
                                }
                            }
                            Validations.check(!durableCJFound, retCodeConfig.getDurableEQPDrbCtrljobUnmatch(), startDurable.getDurableId().getValue());
                        }
                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                        aMachineList = aReticlePod.getQueuedMachines();
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*-------------------------------------------*/
                            /*   1. Check ReticleDispatchJob existence   */
                            /*-------------------------------------------*/
                            List<Infos.ReticleDispatchJob> reticleDispatchJobCheckExistenceResult = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, new ObjectIdentifier(), startDurable.getDurableId(), new ObjectIdentifier());

                        }
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aReticlePod.getDurableControlJob();
                        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());
                        }
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*-------------------------------------------------------------------------*/
                            /*   Check ReticlePod's DurableControlJobID vs Eqp's DurableControlJobID   */
                            /*-------------------------------------------------------------------------*/
                            Validations.check(aDurableControlJob == null, retCodeConfig.getDurableControlJobBlank());

                            ObjectIdentifier durableCJID = new ObjectIdentifier(aDurableControlJob.getIdentifier(), aDurableControlJob.getPrimaryKey());
                            List<ObjectIdentifier> reservedDurableControlJobIDs = equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, strInParam.getEquipmentId());
                            Boolean durableCJFound = false;
                            for (int reservedDurableCJCnt = 0; reservedDurableCJCnt < reservedDurableControlJobIDs.size(); reservedDurableCJCnt++) {
                                if (ObjectIdentifier.equalsWithValue(durableCJID, reservedDurableControlJobIDs.get(reservedDurableCJCnt))) {
                                    durableCJFound = true;
                                    break;
                                }
                            }
                            Validations.check(!durableCJFound, new OmCode(retCodeConfig.getDurableEQPDrbCtrljobUnmatch(), startDurable.getDurableId().getValue()));
                        }
                        /*----------------------------------------*/
                        /*   Check ReticlePod's Transfer Status   */
                        /*----------------------------------------*/
                        String transferStatus = aReticlePod.getTransferStatus();
                        /*===== for OpeStart =====*/
                        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            if (!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                Validations.check(true, new OmCode(retCodeConfig.getInvalidReticlepodXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                            }
                        }
                        /*===== for StartReservation =====*/
                        else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            if (BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetResult = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, strInParam.getEquipmentId());

                                // ReticlePod is loaded by specified port
                                List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = equipmentReticlePodPortInfoGetResult.getReticlePodPortInfoList();
                                int rppLen = CimArrayUtils.getSize(reticlePodPortInfoList);
                                for (int rtclPodPortCnt = 0; rtclPodPortCnt < rppLen; rtclPodPortCnt++) {
                                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfoList.get(rtclPodPortCnt).getReticlePodPortID(), startDurable.getStartDurablePort().getLoadPortID())) {
                                        if (!ObjectIdentifier.equalsWithValue(startDurable.getDurableId(), reticlePodPortInfoList.get(rtclPodPortCnt).getLoadedReticlePodID())) {
                                            Validations.check(true, new OmCode(retCodeConfig.getReticlepodLoadedReticlepod(), startDurable.getDurableId().getValue(),
                                                    reticlePodPortInfoList.get(rtclPodPortCnt).getReticlePodPortID().getValue(), strInParam.getEquipmentId().getValue(),
                                                    reticlePodPortInfoList.get(rtclPodPortCnt).getReticlePodPortID().getValue()));
                                        }
                                    }
                                }
                            }
                        }
                        if (StandardProperties.OM_DRBL_CHK_FOR_EMPTY_POD.getIntValue() == 0) {
                            /*----------------------------------*/
                            /*   Check reticlePod is Empty      */
                            /*----------------------------------*/
                            boolean bEmpty = aReticlePod.isEmpty();
                            Validations.check(!bEmpty, new OmCode(retCodeConfig.getReticlepodNotEmpty(), startDurable.getDurableId().getValue()));
                        }

                    } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
                        aMachineList = aReticle.getQueuedMachines();
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*-------------------------------------------*/
                            /*   1. Check ReticleDispatchJob existence   */
                            /*-------------------------------------------*/
                            List<Infos.ReticleDispatchJob> reticleDispatchJobCheckExistenceDRResult = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, startDurable.getDurableId(), new ObjectIdentifier(), new ObjectIdentifier());
                        }
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aReticle.getDurableControlJob();
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            Validations.check(null != aDurableControlJob, retCodeConfig.getDurableControlJobFilled());
                        }
                        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            /*----------------------------------------------------------------------*/
                            /*   Check Reticle's DurableControlJobID vs Eqp's DurableControlJobID   */
                            /*----------------------------------------------------------------------*/
                            Validations.check(aDurableControlJob == null, retCodeConfig.getDurableControlJobBlank());

                            ObjectIdentifier durableCJID = new ObjectIdentifier(aDurableControlJob.getIdentifier(), aDurableControlJob.getPrimaryKey());
                            List<ObjectIdentifier> reservedDurableControlJobIDs = equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, strInParam.getEquipmentId());
                            Boolean durableCJFound = false;
                            for (int reservedDurableCJCnt = 0; reservedDurableCJCnt < reservedDurableControlJobIDs.size(); reservedDurableCJCnt++) {
                                if (ObjectIdentifier.equalsWithValue(durableCJID, reservedDurableControlJobIDs.get(reservedDurableCJCnt))) {
                                    durableCJFound = true;
                                    break;
                                }
                            }
                            Validations.check(!durableCJFound, new OmCode(retCodeConfig.getDurableEQPDrbCtrljobUnmatch(), startDurable.getDurableId().getValue()));

                        }
                        /*----------------------------------------*/
                        /*   Check Reticle's Transfer Status      */
                        /*----------------------------------------*/
                        String transferStatus = aReticle.getTransportState();
                        /*===== for OpeStart =====*/
                        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
                            Validations.check(!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidReticleXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                        }
                        /*===== for StartReservation =====*/
                        else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)) {
                            if (BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                                List<Infos.StoredReticle> storedReticleList = equipmentMethod.equipmentStoredReticleGetDR(objCommon, strInParam.getEquipmentId());

                                // Reticle is in the specified equipment
                                Boolean bReticleFoundInEqp = false;
                                int srLen = CimArrayUtils.getSize(storedReticleList);
                                for (int srCnt = 0; srCnt < srLen; srCnt++) {
                                    if (ObjectIdentifier.equalsWithValue(startDurable.getDurableId(), storedReticleList.get(srCnt).getReticleID())) {
                                        bReticleFoundInEqp = true;
                                        break;
                                    }
                                }
                                Validations.check(!bReticleFoundInEqp, new OmCode(retCodeConfig.getReticleNotInTheEqp(), startDurable.getDurableId().getValue()));
                            }
                        }
                    }
                    String durableInventoryStateGetResult = this.durableInventoryStateGet(objCommon, durableCategory, startDurable.getDurableId());
                    Validations.check(BizConstant.equalsIgnoreCase(durableInventoryStateGetResult, BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK), new OmCode(retCodeConfig.getInvalidDurableInventoryStat(), startDurable.getDurableId().getValue(), durableInventoryStateGetResult));

                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            String objDurableHoldStateGetOutRetCode = this.durableHoldStateGet(objCommon, durableCategory, startDurable.getDurableId());

                            Validations.check(!BizConstant.equalsIgnoreCase(objDurableHoldStateGetOutRetCode, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD), new OmCode(retCodeConfig.getInvalidDurableHoldStat(), startDurable.getDurableId().getValue(), objDurableHoldStateGetOutRetCode));

                            String objDurableProcessStateGetOutRetCode = this.durableProcessStateGet(objCommon, durableCategory, startDurable.getDurableId());

                            Validations.check(!BizConstant.equalsIgnoreCase(objDurableProcessStateGetOutRetCode, BizConstant.SP_DURABLE_PROCSTATE_WAITING), new OmCode(retCodeConfig.getInvalidDurableProcStat(), startDurable.getDurableId().getValue(), objDurableHoldStateGetOutRetCode));

                            Boolean matchFlag = false;
                            int machineLen = CimArrayUtils.getSize(aMachineList);
                            for (int machineSeq = 0; machineSeq < machineLen; machineSeq++) {
                                ObjectIdentifier qEquipmentID = new ObjectIdentifier(aMachineList.get(machineSeq).getIdentifier(), aMachineList.get(machineSeq).getPrimaryKey());
                                if (ObjectIdentifier.equalsWithValue(qEquipmentID, strInParam.getEquipmentId())) {
                                    matchFlag = true;
                                    break;
                                }

                            }
                            Validations.check(!matchFlag, retCodeConfig.getNotCorrectEqpForOpestartForDurable());

                            try {
                                this.durableCheckEndBankIn(objCommon, durableCategory, startDurable.getDurableId());
                            } catch (ServiceException ex) {
                                if (Validations.isEquals(ex.getCode(), retCodeConfig.getBankinOperation())) {
                                    throw ex;
                                }
                            }

                            //-----------------------//
                            //   Check Route/OpeNo   //
                            //-----------------------//
                            if (!ObjectIdentifier.isEmptyWithValue(startDurable.getStartOperationInfo().getProcessFlowID())
                                    && !CimObjectUtils.isEmpty(startDurable.getStartOperationInfo().getOperationNumber())) {
                                Outputs.ObjDurableCurrentOperationInfoGetOut objDurableCurrentOperationInfoGetOutRetCode = this.durableCurrentOperationInfoGet(objCommon, durableCategory, startDurable.getDurableId());

                                if (ObjectIdentifier.equalsWithValue(startDurable.getStartOperationInfo().getProcessFlowID(), objDurableCurrentOperationInfoGetOutRetCode.getRouteID())
                                        && CimStringUtils.equals(startDurable.getStartOperationInfo().getOperationNumber(), objDurableCurrentOperationInfoGetOutRetCode.getOperationNumber())) {
                                    log.info("{} Route/Operation check OK. Go ahead...", startDurable.getStartOperationInfo().getProcessFlowID());
                                } else {
                                    Validations.check(true, retCodeConfig.getInvalidInputParam());
                                }
                            } else {
                                log.info("Route/Operation check skipped. Go ahead...");
                            }
                        }
                    }
                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTARTCANCEL)) {
                    if (BizConstant.equalsIgnoreCase(strInParam.getDurableCategory(), SP_DURABLECAT_CASSETTE)) {
                        Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeGetResult = portMethod.portResourceCurrentOperationModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());

                        if (BizConstant.equalsIgnoreCase(portResourceCurrentOperationModeGetResult.getOperationMode().getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                            String transferState = aCassette.getTransportState();
                            Validations.check(!BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                        }
                    } else if (BizConstant.equalsIgnoreCase(strInParam.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
                        Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut objReticlePodPortResourceCurrentAccessModeGetOutRetCode = reticleMethod.reticlePodPortResourceCurrentAccessModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());
                        if (BizConstant.equalsIgnoreCase(objReticlePodPortResourceCurrentAccessModeGetOutRetCode.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                            String transferStatus = aReticlePod.getTransferStatus();
                            Validations.check(!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidReticlepodXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                        }
                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                        String transferStatus = aReticle.getTransportState();
                        Validations.check(!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidReticleXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                    }

                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            String durableHoldStateGetResult = this.durableHoldStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!BizConstant.equalsIgnoreCase(durableHoldStateGetResult, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD), new OmCode(retCodeConfig.getInvalidDurableHoldStat(), startDurable.getDurableId().getValue(), durableHoldStateGetResult));

                            String durableProcessStateGetResult = this.durableProcessStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!BizConstant.equalsIgnoreCase(durableProcessStateGetResult, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING), new OmCode(retCodeConfig.getInvalidDurableProcStat(), startDurable.getDurableId().getValue(), durableProcessStateGetResult));

                        }
                    }

                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPERATIONCOMP)) {
                    String transferStatus = null;
                    String accessMode = null;
                    String onlineMode = null;
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeGetResult = portMethod.portResourceCurrentOperationModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());

                        accessMode = portResourceCurrentOperationModeGetResult.getOperationMode().getAccessMode();
                        onlineMode = portResourceCurrentOperationModeGetResult.getOperationMode().getOnlineMode();
                        transferStatus = aCassette.getTransportState();
                    } else {
                        /*-----------------------------------------*/
                        /*   Get Equipment's Operation Mode Info   */
                        /*-----------------------------------------*/
                        String equipmentOnlineModeGetResult = equipmentMethod.equipmentOnlineModeGet(objCommon, strInParam.getEquipmentId());

                        onlineMode = equipmentOnlineModeGetResult;
                        if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                            Outputs.ObjReticlePodPortResourceCurrentAccessModeGetOut objReticlePodPortResourceCurrentAccessModeGetOutRetCode = reticleMethod.reticlePodPortResourceCurrentAccessModeGet(objCommon, strInParam.getEquipmentId(), startDurable.getStartDurablePort().getLoadPortID());

                            accessMode = objReticlePodPortResourceCurrentAccessModeGetOutRetCode.getAccessMode();
                            transferStatus = aReticlePod.getTransferStatus();
                        } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                            transferStatus = aReticle.getTransportState();
                        }
                    }
                    if (BizConstant.equalsIgnoreCase(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                            || BizConstant.equalsIgnoreCase(accessMode, BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                        Validations.check(!BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), retCodeConfig.getInvalidTransferState());
                    }
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        //-----------------------------------------------------------
                        // Check cassette interFabXferState
                        //-----------------------------------------------------------
                        String cassetteInterFabTransferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommon, startDurable.getDurableId());
                        Validations.check(CimStringUtils.equals(cassetteInterFabTransferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                                retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(),
                                startDurable.getDurableId().getValue(),
                                cassetteInterFabTransferStateGetResult);
                    }
                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            String durableHoldStateGetResult = this.durableHoldStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!BizConstant.equalsIgnoreCase(durableHoldStateGetResult, BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD),
                                    new OmCode(retCodeConfig.getInvalidDurableHoldStat(), startDurable.getDurableId().getValue(), durableHoldStateGetResult));

                            String durableProcessStateGetResult = this.durableProcessStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(!BizConstant.equalsIgnoreCase(durableProcessStateGetResult, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING),
                                    new OmCode(retCodeConfig.getInvalidDurableProcStat(), startDurable.getDurableId().getValue(), durableProcessStateGetResult));
                        }
                    }

                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL)) {
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        /*----------------------------------------------*/
                        /*   Get and Check Cassette's Dispatch Status   */
                        /*----------------------------------------------*/
                        Boolean dispatchReserveFlag = aCassette.isDispatchReserved();
                        Validations.check(!dispatchReserveFlag, retCodeConfig.getNotDispatchReservedCassette());
                    }
                    try {
                        this.durableOnRouteCheck(objCommon, durableCategory, startDurable.getDurableId());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                            String durableProcessStateGetResult = this.durableProcessStateGet(objCommon, durableCategory, startDurable.getDurableId());
                            Validations.check(BizConstant.equalsIgnoreCase(durableProcessStateGetResult, BizConstant.SP_DURABLE_PROCSTATE_PROCESSING),
                                    new OmCode(retCodeConfig.getInvalidDurableProcStat(), startDurable.getDurableId().getValue(), durableProcessStateGetResult));
                        }

                    }

                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_LOADING)) {
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        /*-------------------------------*/
                        /*   Check SorterJob existence   */
                        /*-------------------------------*/
                        List<ObjectIdentifier> dummyIDs = new ArrayList<>();
                        Infos.EquipmentLoadPortAttribute equipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
                        List<Infos.CassetteLoadPort> cassetteLoadPortList = new ArrayList<>();
                        equipmentLoadPortAttribute.setCassetteLoadPortList(cassetteLoadPortList);
                        Infos.CassetteLoadPort cassetteLoadPort = new Infos.CassetteLoadPort();
                        cassetteLoadPortList.add(cassetteLoadPort);
                        cassetteLoadPort.setPortID(null == startDurable.getStartDurablePort() ? null : startDurable.getStartDurablePort().getLoadPortID());
                        cassetteLoadPort.setCassetteID(startDurable.getDurableId());
                        equipmentLoadPortAttribute.setEquipmentID(strInParam.getEquipmentId());

                        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
                        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(equipmentLoadPortAttribute);
                        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyIDs);
                        objWaferSorterJobCheckForOperation.setLotIDList(dummyIDs);
                        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_LOADINGLOT);
                        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
                        /*--------------------------------------*/
                        /*   Check Cassette's Transfer Status   */
                        /*--------------------------------------*/

                        /*-----------------------*/
                        /*   Get TransferState   */
                        /*-----------------------*/
                        String transferState = aCassette.getTransportState();
                        if (BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_STATIONOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_MANUALOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_SHELFOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_TRANSSTATE_ABNORMALOUT)
                                || BizConstant.equalsIgnoreCase(transferState, BizConstant.SP_UNDEFINED_STATE)) {
                            log.info("transferState == {}", transferState);
                        } else {
                            Validations.check(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startDurable.getDurableId().getValue()));
                        }
                        //-----------------------------------------------------------
                        // Check cassette interFabXferState
                        //-----------------------------------------------------------
                        String cassetteInterFabTransferStateGetResult = cassetteMethod.cassetteInterFabXferStateGet(objCommon, startDurable.getDurableId());
                        Validations.check(CimStringUtils.equals(cassetteInterFabTransferStateGetResult, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                                new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), startDurable.getDurableId().getValue(), cassetteInterFabTransferStateGetResult));
                        if (StandardProperties.OM_DRBL_CHK_FOR_EMPTY_CARRIER.getIntValue() == 0) {

                            /*--------------------------------*/
                            /*   Check Cassette is Empty      */
                            /*--------------------------------*/
                            Boolean bEmpty = aCassette.isEmpty();
                            Validations.check(!bEmpty, new OmCode(retCodeConfig.getCastNotEmpty(), startDurable.getDurableId().getValue()));
                        }
                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                        /*----------------------------------------*/
                        /*   Check ReticlePod's Transfer Status   */
                        /*----------------------------------------*/
                        String transferStatus = aReticlePod.getTransferStatus();
                        if (BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_UNDEFINED_STATE)) {
                            log.info("transferStatus == {}", transferStatus);
                        } else {
                            Validations.check(true, new OmCode(retCodeConfig.getInvalidReticlepodXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                        }
                        if (StandardProperties.OM_DRBL_CHK_FOR_EMPTY_POD.getIntValue() == 0) {
                            /*----------------------------------*/
                            /*   Check reticlePod is Empty      */
                            /*----------------------------------*/
                            boolean bEmpty = aReticlePod.isEmpty();
                            Validations.check(!bEmpty, new OmCode(retCodeConfig.getReticlepodNotEmpty(), startDurable.getDurableId().getValue()));
                        }
                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                        /*----------------------------------------*/
                        /*   Check Reticle's Transfer Status      */
                        /*----------------------------------------*/
                        String transferStatus = aReticle.getTransportState();
                        if (BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT)
                                || BizConstant.equalsIgnoreCase(transferStatus, BizConstant.SP_UNDEFINED_STATE)) {
                            log.info("transferStatus == {}", transferStatus);
                        } else {
                            Validations.check(true, new OmCode(retCodeConfig.getInvalidReticleXferStat(), startDurable.getDurableId().getValue(), transferStatus));
                        }
                    }
                } else if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_PFXDELETE)) {
                    if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aCassette.getDurableControlJob();
                        Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());
                    } else if (BizConstant.equalsIgnoreCase(strInParam.getDurableCategory(), SP_DURABLECAT_RETICLEPOD)) {
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aReticlePod.getDurableControlJob();
                        Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());

                    } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
                        /*---------------------------------------*/
                        /*   Get and Check DurableControlJobID   */
                        /*---------------------------------------*/
                        CimDurableControlJob aDurableControlJob = aReticle.getDurableControlJob();
                        Validations.check(aDurableControlJob != null, retCodeConfig.getDurableControlJobFilled());

                    }
                    //---------------------------------------
                    //  Get InPostProcessFlag of Cassette
                    //---------------------------------------
                    Boolean durableInPostProcessFlagGetResult = this.durableInPostProcessFlagGet(objCommon, durableCategory, startDurable.getDurableId());
                    Validations.check(durableInPostProcessFlagGetResult, new OmCode(retCodeConfig.getDurableInPostProcess(), startDurable.getDurableId().getValue()));

                }
                durableCnt++;
            }
        }
        /*-------------------------------------------------------*/
        /*   Check Upper/Lower Limit for RecipeParameterChange   */
        /*-------------------------------------------------------*/
        if (BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)
                || BizConstant.equalsIgnoreCase(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, strInParam.getEquipmentId());
            List<RecipeDTO.RecipeParameter> aRecipeParameters = aMachine.getRecipeParameters();
            if (!CimObjectUtils.isEmpty(aRecipeParameters)) {
                for (RecipeDTO.RecipeParameter recipeParameter : aRecipeParameters) {
                    if (BizConstant.equalsIgnoreCase(recipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_STRING)) {
                        log.info("########## dataType is SP_DCDef_Val_String -----> continue !!");
                        continue;
                    }
                    boolean bRcpParamFound = false;
                    int startRcpParamCnt = 0;
                    List<Infos.StartRecipeParameter> startRecipeParameters = durableStartRecipe.getStartRecipeParameterS();
                    for (startRcpParamCnt = 0; startRcpParamCnt < startRecipeParameters.size(); startRcpParamCnt++) {
                        if (CimStringUtils.equals(startRecipeParameters.get(startRcpParamCnt).getParameterName(), recipeParameter.getParameterName())) {
                            bRcpParamFound = true;
                            break;
                        }
                    }
                    if (bRcpParamFound) {
                        if (recipeParameter.getUseCurrentValueFlag()) {
                            Validations.check(!CimObjectUtils.isEmpty(startRecipeParameters.get(startRcpParamCnt).getParameterValue()), new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), recipeParameter.getParameterName()));
                        } else {
                            if (BizConstant.equalsIgnoreCase(recipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                String parameterValueStr = startRecipeParameters.get(startRcpParamCnt).getParameterValue();
                                Long lowerLimit = Long.parseLong(recipeParameter.getLowerLimit());
                                Long upperLimit = Long.parseLong(recipeParameter.getUpperLimit());
                                Long parameterValue = Long.parseLong(parameterValueStr);
                                Validations.check((parameterValue < lowerLimit || parameterValue > upperLimit),
                                        new OmCode(retCodeConfig.getInvalidParameterValueRange(), recipeParameter.getParameterName(), recipeParameter.getLowerLimit(), recipeParameter.getUpperLimit()));
                            } else if (BizConstant.equalsIgnoreCase(recipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                double parameterValue = Double.parseDouble(startRecipeParameters.get(startRcpParamCnt).getParameterValue());
                                double lowerLimit = Double.parseDouble(recipeParameter.getLowerLimit());
                                double upperLimit = Double.parseDouble(recipeParameter.getUpperLimit());
                                Validations.check(parameterValue < lowerLimit || parameterValue > upperLimit, new OmCode(retCodeConfig.getInvalidParameterValueRange(), recipeParameter.getParameterName(), recipeParameter.getLowerLimit(), recipeParameter.getUpperLimit()));
                            }
                        }
                    }
                }
            }
        }
        /*---------------------------------------------------------------------*/
        /*   Get and Check Entity Inhibition for OpeStart / StartReservation   */
        /*---------------------------------------------------------------------*/
        if (CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(strInParam.getOperation(), BizConstant.SP_OPERATION_OPESTART)) {
            List<ObjectIdentifier> durableIDs = startDurableList.stream().map(startDurable -> startDurable.getDurableId()).collect(Collectors.toList());
            Inputs.ObjEquipmentCheckInhibitForDurableWithMachineRecipeIn objEquipmentCheckInhibitForDurableWithMachineRecipeIn = new Inputs.ObjEquipmentCheckInhibitForDurableWithMachineRecipeIn();
            objEquipmentCheckInhibitForDurableWithMachineRecipeIn.setEquipmentID(strInParam.getEquipmentId());
            objEquipmentCheckInhibitForDurableWithMachineRecipeIn.setDurableIDs(durableIDs);
            objEquipmentCheckInhibitForDurableWithMachineRecipeIn.setDurableCategory(strInParam.getDurableCategory());
            objEquipmentCheckInhibitForDurableWithMachineRecipeIn.setDurableStartRecipe(durableStartRecipe);
            equipmentMethod.equipmentCheckInhibitForDurableWithMachineRecipe(objCommon, objEquipmentCheckInhibitForDurableWithMachineRecipeIn);

        }
    }

    @Override
    public Outputs.ObjDurableRecipeGetOut durableRecipeGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, ObjectIdentifier equipmentID) {
        /*---------------------------*/
        /*   get PosMachine object   */
        /*---------------------------*/
        CimMachine machineBO = baseCoreFactory.getBO(CimMachine.class, equipmentID);

        CimDurableProcessOperation durablePOObj = null;
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);

            durablePOObj = aCassette.getDurableProcessOperation();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            durablePOObj = aReticlePod.getDurableProcessOperation();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            durablePOObj = aReticle.getDurableProcessOperation();
        }

        Validations.check(durablePOObj == null, new OmCode(retCodeConfig.getNotFoundDurablePo(), durableID.getValue()));

        CimProductSpecification nilObj = null;
        CimLogicalRecipe logicalRecipeObj = durablePOObj.findLogicalRecipeFor(nilObj);
        Validations.check(logicalRecipeObj == null, retCodeConfig.getNotFoundLogicalRecipe());

        String subLotType = "";
        CimMachineRecipe machineRecipeObj = logicalRecipeObj.findMachineRecipeForSubLotType(machineBO, subLotType);
        Validations.check(machineRecipeObj == null, retCodeConfig.getNotFoundMachineRecipe());

        ObjectIdentifier logicalRecipeID = new ObjectIdentifier(logicalRecipeObj.getIdentifier(), logicalRecipeObj.getPrimaryKey());
        ObjectIdentifier machineRecipeID = new ObjectIdentifier(machineRecipeObj.getIdentifier(), machineRecipeObj.getPrimaryKey());

        Outputs.ObjDurableRecipeGetOut objDurableRecipeGetOut = new Outputs.ObjDurableRecipeGetOut();
        objDurableRecipeGetOut.setLogicalRecipeID(logicalRecipeID);
        objDurableRecipeGetOut.setMachineRecipeID(machineRecipeID);
        return objDurableRecipeGetOut;
    }

    @Override
    public void durableStatusCheckForOperation(Infos.ObjCommon objCommon, String operation, ObjectIdentifier durableID, String durableCategory) {
        //--------------//
        //  Initialize  //
        //--------------//
        if (!CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)
                && !CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            throw new ServiceException(retCodeConfig.getInvalidDurableCategory(), durableCategory);
        }
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_PFXCREATE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_PFXDELETE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_DURABLECONTROLJOBMANAGE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_LOADING)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATIONCATEGORY_DURABLEHOLD)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATIONCATEGORY_REWORK)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_LOCATE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATIONCATEGORY_GATEPASS)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_BRSCRIPT)) {
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_DURABLECONTROLJOBMANAGE)) {
                if (!CimStringUtils.equals("ODRBW022", objCommon.getTransactionID())) {
                    return;
                }
            } else if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_BRSCRIPT)) {
                if (!CimStringUtils.equals("TXPDC059", objCommon.getTransactionID())) {
                    return;
                }
            }
            if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aCassette, new OmCode(retCodeConfig.getNotFoundCassette(), durableID.getValue()));
                com.fa.cim.newcore.bo.durable.CimDurableSubState durableSubState = aCassette.getDurableSubState();
                if (null != durableSubState) {
                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                            || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
                        Boolean durableProcessAvailableFlag = durableSubState.isDurableProcessAvailable();
                        Validations.check(!durableProcessAvailableFlag,
                                new OmCode(retCodeConfig.getDurableNotAvailableStateForDrblProcess(), durableID.getValue()));
                    }
                } else {
                    String durableState = aCassette.getDurableState();
                    Validations.check(!CimStringUtils.equals(durableState, BizConstant.CIMFW_DURABLE_NOTAVAILABLE),
                            new OmCode(retCodeConfig.getInvalidCassetteState(), durableState, durableID.getValue()));
                }
            } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aReticlePod, new OmCode(retCodeConfig.getNotFoundReticlePod(), durableID.getValue()));
                com.fa.cim.newcore.bo.durable.CimDurableSubState durableSubState = aReticlePod.getDurableSubState();
                if (null != durableSubState) {
                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                            || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
                        Boolean durableProcessAvailableFlag = durableSubState.isDurableProcessAvailable();
                        Validations.check(!durableProcessAvailableFlag,
                                new OmCode(retCodeConfig.getDurableNotAvailableStateForDrblProcess(), durableID.getValue()));
                    }
                } else {
                    Boolean isNotAvailableFlag = aReticlePod.isNotAvailable();
                    Validations.check(!isNotAvailableFlag, new OmCode(retCodeConfig.getInvalidReticlepodStat(), "*****", durableID.getValue()));
                }  //DSN000101569
            } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
                CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == aReticle, new OmCode(retCodeConfig.getNotFoundReticle(), durableID.getValue()));
                com.fa.cim.newcore.bo.durable.CimDurableSubState durableSubState = aReticle.getDurableSubState();
                if (null != durableSubState) {
                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                            || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
                        Boolean durableProcessAvailableFlag = durableSubState.isDurableProcessAvailable();
                        Validations.check(!durableProcessAvailableFlag,
                                new OmCode(retCodeConfig.getDurableNotAvailableStateForDrblProcess(), durableID.getValue()));
                    }
                } else {
                    String durableState = aReticle.getDurableState();
                    Validations.check(!CimStringUtils.equals(durableState, BizConstant.CIMFW_DURABLE_NOTAVAILABLE),
                            new OmCode(retCodeConfig.getInvalidReticlepodStat(), durableID.getValue(), durableState));
                }
            }
        } else if (CimStringUtils.equals(operation, BizConstant.SP_OPERATIONCATEGORY_BANKIN)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATIONCATEGORY_MOVEBANK)) {
            if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aCassette, new OmCode(retCodeConfig.getNotFoundCassette(), durableID.getValue()));
                com.fa.cim.newcore.bo.durable.CimDurableSubState durableSubState = aCassette.getDurableSubState();
                if (null == durableSubState) {
                    String durableState = aCassette.getDurableState();
                    Validations.check(!CimStringUtils.equals(durableState, BizConstant.CIMFW_DURABLE_NOTAVAILABLE) && !CimStringUtils.equals(durableState, BizConstant.CIMFW_DURABLE_AVAILABLE),
                            new OmCode(retCodeConfig.getInvalidCassetteState(), durableState, durableID.getValue()));
                }
            } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
                CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aReticlePod, new OmCode(retCodeConfig.getNotFoundReticlePod(), durableID.getValue()));
                com.fa.cim.newcore.bo.durable.CimDurableSubState durableSubState = aReticlePod.getDurableSubState();
                if (null == durableSubState) {
                    Boolean isNotAvailableFlag = aReticlePod.isNotAvailable();
                    Boolean isAvailableFlag = aReticlePod.isAvailable();
                    Validations.check(!isNotAvailableFlag && !isAvailableFlag, new OmCode(retCodeConfig.getInvalidReticlepodStat(), "*****", durableID.getValue()));
                }
            } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
                CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(aReticle == null, new OmCode(retCodeConfig.getNotFoundReticle(), durableID.getValue()));
                com.fa.cim.newcore.bo.durable.CimDurableSubState durableSubState = aReticle.getDurableSubState();
                if (null == durableSubState) {
                    String durableState = aReticle.getDurableState();
                    Validations.check(!CimStringUtils.equals(durableState, BizConstant.CIMFW_DURABLE_NOTAVAILABLE)
                                    && !CimStringUtils.equals(durableState, BizConstant.CIMFW_DURABLE_AVAILABLE),
                            new OmCode(retCodeConfig.getInvalidReticleStat(), durableID.getValue(), durableState));
                }
            }
        } else if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPERATIONCOMP)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTARTCANCEL)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATIONCATEGORY_BANKINCANCEL)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATIONCATEGORY_REWORKCANCEL)) {
        } else {
            throw new ServiceException(retCodeConfig.getInvalidOperationType());
        }
    }

    @Override
    public List<ObjectIdentifier> durableCapabilityIDGetDR(Infos.ObjCommon objCommon, String category) {
        log.info("【Method Entry】durableCapabilityIDGetDR()");
        List<CimDurableGroupDO> durableGroupList = cimJpaRepository.query("SELECT PDRBL_GRP_ID, ID FROM OMPDRBLGRP WHERE PDRBL_TYPE = ?1 ORDER BY PDRBL_GRP_ID", CimDurableGroupDO.class, category);

        List<ObjectIdentifier> durableCapabilityIDs = new ArrayList<>();
        int count = CimArrayUtils.getSize(durableGroupList);
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                CimDurableGroupDO durableGroup = durableGroupList.get(i);
                ObjectIdentifier objectIdentifier = new ObjectIdentifier(durableGroup.getDurableGroupId(), durableGroup.getId());
                durableCapabilityIDs.add(objectIdentifier);
            }
        }

        log.info("【Method Exit】durableCapabilityIDGetDR()");
        return durableCapabilityIDs;
    }

    @Override
    public List<ObjectIdentifier> durableIDGetDR(Infos.ObjCommon objCommon, String category) {
        List<CimDurableDO> durables = cimJpaRepository.query("SELECT PDRBL_ID, ID FROM OMPDRBL WHERE PDRBL_CATEGORY = ?1 ORDER BY PDRBL_ID", CimDurableDO.class, category);
        return CimObjectUtils.isEmpty(durables) ? Collections.emptyList() : durables.stream().map(x -> new ObjectIdentifier(x.getDurableId(), x.getId())).collect(Collectors.toList());
    }

    @Override
    public void durableCheckForUpdate(Infos.ObjCommon objCommon, String className, Infos.DurableAttribute registAttribute) {
        /* Check whether the target object can be updated or not */
        if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE, className)) {
            String cassetteInfoCheckFlag = StandardProperties.OM_CARRIER_BASE_INFO_CHECK.getValue();
            if (CimStringUtils.equals(BizConstant.SP_CHECKFLAG_ON, cassetteInfoCheckFlag)) {

                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, registAttribute.getDurableID());

                int maximumRunTime = 0;
                if (CimStringUtils.isEmpty(registAttribute.getMaximumRunTime())) {
                    maximumRunTime = CimNumberUtils.intValue(registAttribute.getMaximumRunTime());
                }

                DurableDTO.BrCarrierInfo cassetteInfo = new DurableDTO.BrCarrierInfo();
                cassetteInfo.setDescription(registAttribute.getDescription());
                cassetteInfo.setInstanceName(registAttribute.getInstanceName());
                cassetteInfo.setCarrierCategory(ObjectIdentifier.build(null, registAttribute.getCategory()));
                cassetteInfo.setUsageCheckRequiredFlag(registAttribute.getUsageCheckFlag());
                cassetteInfo.setMaximumRunTime(maximumRunTime);
                cassetteInfo.setMaximumStartCount(registAttribute.getMaximumOperationStartCount().intValue());
                cassetteInfo.setCapacity(registAttribute.getCapacity());
                cassetteInfo.setNominalSize(registAttribute.getNominalSize());
                cassetteInfo.setContents(registAttribute.getContents());
                cassetteInfo.setIntervalBetweenPM(registAttribute.getIntervalBetweenPM());
                cassette.checkForCassetteUpdate(cassetteInfo, BizConstant.SP_CHECK_MSG_DEFAULT_MAX_COUNT.intValue());
            }
        } else if (CimStringUtils.equals(SP_DURABLECAT_RETICLEPOD, className)) {
            String cassetteInfoCheckFlag = StandardProperties.OM_RTCLPOD_BASE_INFO_CHECK.getValue();
            if (CimStringUtils.equals(BizConstant.SP_CHECKFLAG_ON, cassetteInfoCheckFlag)) {
                CimReticlePod reticlePodBO = baseCoreFactory.getBO(CimReticlePod.class, registAttribute.getDurableID());

                DurableDTO.ReticlePodInfo reticlePodInfo = new DurableDTO.ReticlePodInfo();
                reticlePodInfo.setDescription(registAttribute.getDescription());
                reticlePodInfo.setInstanceName(registAttribute.getInstanceName());
                reticlePodInfo.setIntervalBetweenPM(registAttribute.getIntervalBetweenPM());
                reticlePodInfo.setReticlePodCategory(new ObjectIdentifier(registAttribute.getCategory()));
                reticlePodInfo.setSlotPositionCount(registAttribute.getCapacity());
                reticlePodBO.checkForReticlePodUpdate(reticlePodInfo, BizConstant.SP_CHECK_MSG_DEFAULT_MAX_COUNT);
            }
        }
    }

    @Override
    public void durableSettingCheck(Infos.ObjCommon objCommon, String className, Infos.DurableAttribute registAttribute) {
        String registeredInstanceName = null;

        if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE, className)) {
            CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, registAttribute.getDurableID());

            int registeredCassetteCapacity = cassette.getCapacity();
            Validations.check(registAttribute.getCapacity() != registeredCassetteCapacity, retCodeConfig.getReferenceValueDiffer(), objCommon.getTransactionID());

            int registeredNominalSize = cassette.getNominalSize();
            Validations.check(registAttribute.getNominalSize() != registeredNominalSize, retCodeConfig.getReferenceValueDiffer(), objCommon.getTransactionID());
            registeredInstanceName = cassette.getInstanceName();

        } else if (CimStringUtils.equals(SP_DURABLECAT_RETICLEPOD, className)) {
            CimReticlePod reticlePod = baseCoreFactory.getBO(CimReticlePod.class, registAttribute.getDurableID());

            int registeredReticlePodCapacity = reticlePod.getCapacity();
            Validations.check(registAttribute.getCapacity() != registeredReticlePodCapacity, retCodeConfig.getReferenceValueDiffer(), objCommon.getTransactionID());

            registeredInstanceName = reticlePod.getInstanceName();
        }

        String checkInstanceName = null;
        if (CimStringUtils.isNotEmpty(registeredInstanceName)) {
            if (!registeredInstanceName.equals(registAttribute.getInstanceName())) {
                checkInstanceName = registeredInstanceName;
            }
        } else if (CimStringUtils.isNotEmpty(registAttribute.getInstanceName())) {
            String instanceName = StandardProperties.OM_INSTANCE_ID.getValue();
            if (!instanceName.equals(registAttribute.getInstanceName())) {
                checkInstanceName = registeredInstanceName;
            }
        }

        Validations.check(CimStringUtils.isNotEmpty(checkInstanceName), retCodeConfig.getReferenceValueDiffer(), objCommon.getTransactionID());

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param updateFlag
     * @param className
     * @param registAttributeWithUdata
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2019/2/19 15:22:18
     */
    @Override
    public void durableRegist(Infos.ObjCommon objCommon, boolean updateFlag, String className, Infos.DurableAttribute registAttributeWithUdata) {
        if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE, className)) {
            CimCassette aCassette = null;
            if (CimBooleanUtils.isTrue(updateFlag)) {
                aCassette = baseCoreFactory.getBO(CimCassette.class, registAttributeWithUdata.getDurableID());
                //get current cast usage type
                String currentProductUsage = aCassette.getProductUsage();
                //check if the usage type is updated
                String usageType = registAttributeWithUdata.getProductUsage();
                boolean usageUpdateFlag = CimStringUtils.equals(currentProductUsage,usageType);
                if (usageUpdateFlag){
                    //check if the cast is not empty
                    Integer usedCapacity = aCassette.getUsedCapacity();
                    Validations.check(usedCapacity > 0 ,retCodeConfigEx.getCarrierNotEmpty());
                }
            } else {
                aCassette = durableManager.createCassetteNamed(registAttributeWithUdata.getDurableID().getValue());
                Validations.check(aCassette == null, retCodeConfig.getNotFoundCassette());
            }

            DurableDTO.CassetteInfo strCassetteInfo = new DurableDTO.CassetteInfo();

            Long maximumRunTime = 0L;
            if (CimStringUtils.length(registAttributeWithUdata.getMaximumRunTime()) > 0) {
                maximumRunTime = CimLongUtils.longValue(registAttributeWithUdata.getMaximumRunTime());
            }

            strCassetteInfo.setDescription(registAttributeWithUdata.getDescription());
            strCassetteInfo.setInstanceName(registAttributeWithUdata.getInstanceName());
            strCassetteInfo.setCarrierCategory(ObjectIdentifier.buildWithValue(registAttributeWithUdata.getCategory()));
            strCassetteInfo.setUsageCheckRequiredFlag(registAttributeWithUdata.getUsageCheckFlag());
            strCassetteInfo.setMaximumRunTime(maximumRunTime.intValue());
            strCassetteInfo.setMaximumStartCount(registAttributeWithUdata.getMaximumOperationStartCount() == null ? null :
                    registAttributeWithUdata.getMaximumOperationStartCount().intValue());
            strCassetteInfo.setCapacity(registAttributeWithUdata.getCapacity());
            strCassetteInfo.setNominalSize(registAttributeWithUdata.getNominalSize());
            strCassetteInfo.setContents(registAttributeWithUdata.getContents());
            strCassetteInfo.setIntervalBetweenPM(registAttributeWithUdata.getIntervalBetweenPM());
            strCassetteInfo.setUsageType(registAttributeWithUdata.getProductUsage());
            strCassetteInfo.setCarrierType(registAttributeWithUdata.getCarrierType());

            List<Infos.UserData> strUserDataSeq = registAttributeWithUdata.getUserDatas();
            int uDataLen = CimArrayUtils.getSize(strUserDataSeq);
            List<GlobalDTO.UserDataSet> userDataSets = new ArrayList<>();
            strCassetteInfo.setUserDataSets(userDataSets);
            for (int i = 0; i < uDataLen; i++) {
                GlobalDTO.UserDataSet userDataSet = new GlobalDTO.UserDataSet();
                userDataSets.add(userDataSet);
                Infos.UserData strUserData = strUserDataSeq.get(i);
                userDataSet.setName(strUserData.getName());
                userDataSet.setType(strUserData.getType());
                userDataSet.setValue(strUserData.getValue());
                userDataSet.setOriginator(strUserData.getOriginator());
            }

            aCassette.setCassetteBaseInfo(strCassetteInfo);
        } else if (CimStringUtils.equals(className, SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod;
            if (CimBooleanUtils.isTrue(updateFlag)) {
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, registAttributeWithUdata.getDurableID());
            } else {
                aReticlePod = durableManager.createReticlePodNamed(registAttributeWithUdata.getDurableID().getValue());
            }

            DurableDTO.ReticlePodInfo strReticlePodInfo = new DurableDTO.ReticlePodInfo();

            strReticlePodInfo.setDescription(registAttributeWithUdata.getDescription());
            strReticlePodInfo.setInstanceName(registAttributeWithUdata.getInstanceName());
            strReticlePodInfo.setReticlePodCategory(ObjectIdentifier.buildWithValue(registAttributeWithUdata.getCategory()));
            strReticlePodInfo.setIntervalBetweenPM(registAttributeWithUdata.getIntervalBetweenPM());
            strReticlePodInfo.setSlotPositionCount(registAttributeWithUdata.getCapacity());
            List<Infos.UserData> strUserDataSeq = registAttributeWithUdata.getUserDatas();
            int uDataLen = CimArrayUtils.getSize(strUserDataSeq);
            List<GlobalDTO.UserDataSet> userDataSets = new ArrayList<>();
            strReticlePodInfo.setUserDataSets(userDataSets);
            for (int i = 0; i < uDataLen; i++) {
                GlobalDTO.UserDataSet userDataSet = new GlobalDTO.UserDataSet();
                userDataSets.add(userDataSet);
                Infos.UserData strUserData = strUserDataSeq.get(i);
                userDataSet.setName(strUserData.getName());
                userDataSet.setType(strUserData.getType());
                userDataSet.setValue(strUserData.getValue());
                userDataSet.setOriginator(strUserData.getOriginator());
            }

            aReticlePod.setReticlePodBaseInfo(strReticlePodInfo);
        }
    }

    @Override
    public List<Infos.CandidateDurableSubStatusDetail> durableFillInTxPDQ034DR(Infos.ObjCommon objCommon, Params.DurableSubStatusSelectionInqParams params) {
        String durableAvailable = BizConstant.CIMFW_DURABLE_AVAILABLE;
        String durableInUse = BizConstant.CIMFW_DURABLE_INUSE;
        String durableNotAvailable = BizConstant.CIMFW_DURABLE_NOTAVAILABLE;
        String durableScrapped = BizConstant.CIMFW_DURABLE_SCRAPPED;
        String durableCategory = params.getDurableCategory();
        ObjectIdentifier durableID = params.getDurableID();
        boolean allInquiryFlag = params.isAllInquiryFlag();
        com.fa.cim.newcore.bo.durable.CimDurableSubState currentDurableSubState = null;
        String currentDurableState = null;
        if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
            currentDurableState = aCassette.getDurableState();
            currentDurableSubState = aCassette.getDurableSubState();

        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            boolean isAvailableFlag = aReticlePod.isAvailable();
            boolean isInUseFlag = aReticlePod.isInUse();
            boolean isNotAvailableFlag = aReticlePod.isNotAvailable();
            boolean isScrappedFlag = aReticlePod.isScrapped();
            currentDurableState = isAvailableFlag ? durableAvailable
                    : (isInUseFlag ? durableInUse
                    : (isNotAvailableFlag ? durableNotAvailable
                    : (isScrappedFlag ? durableScrapped : BizConstant.CIMFW_DURABLE_UNDEFINED)));
            currentDurableSubState = aReticlePod.getDurableSubState();

        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticle), retCodeConfig.getNotFoundReticle(),durableID);
            currentDurableState = aReticle.getDurableState();
            currentDurableSubState = aReticle.getDurableSubState();
        } else {
            Validations.check(true, retCodeConfig.getInvalidDurableCategory());
        }

        boolean changeToOtherFlag = true;
        ObjectIdentifier currentDurableSubStateID = null != currentDurableSubState ?
                ObjectIdentifier.build(currentDurableSubState.getIdentifier(), currentDurableSubState.getPrimaryKey()) :
                ObjectIdentifier.emptyIdentifier();
        List<ObjectIdentifier> durableSubStateTransitions = null;
        if (null != currentDurableSubState) {
            durableSubStateTransitions = currentDurableSubState.allDurableSubStateTransitions(); //currentDurableSubState->allDurableSubStateTransitions();
            changeToOtherFlag = currentDurableSubState.isChangeToOtherDurableState();
        }
        String durableState;
        List<Infos.CandidateDurableSubStatusDetail> candidateDurableSubStatusDetails = new ArrayList<>();
        if (!allInquiryFlag && !CimObjectUtils.isEmpty(durableSubStateTransitions)) {
            List<Infos.CandidateDurableSubStatus> availableStatuses = new ArrayList<>();
            List<Infos.CandidateDurableSubStatus> inUseStatuses = new ArrayList<>();
            List<Infos.CandidateDurableSubStatus> notAvailableStatuses = new ArrayList<>();
            List<Infos.CandidateDurableSubStatus> scrappedStatuses = new ArrayList<>();
            for (ObjectIdentifier trans : durableSubStateTransitions) {
                com.fa.cim.newcore.bo.durable.CimDurableSubState aNewDurableSubState = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimDurableSubState.class, trans);
                Validations.check(null == aNewDurableSubState, retCodeConfig.getNotFoundDurableSubstat());

                durableState = aNewDurableSubState.getDurableState();
                ObjectIdentifier newDurableSubStateID = new ObjectIdentifier(aNewDurableSubState.getIdentifier(), aNewDurableSubState.getPrimaryKey());
                String subStateName = aNewDurableSubState.getDurableSubStateName();
                String subStateDesc = aNewDurableSubState.getDescription();
                Boolean availableForDurableFlag = aNewDurableSubState.isDurableProcessAvailable();
                if (CimStringUtils.equals(BizConstant.CIMFW_DURABLE_AVAILABLE, durableState)) {
                    availableStatuses.add(new Infos.CandidateDurableSubStatus(newDurableSubStateID, subStateName, subStateDesc, availableForDurableFlag));
                } else if (CimStringUtils.equals(BizConstant.CIMFW_DURABLE_INUSE, durableState)) {
                    if (currentDurableState.equals(durableAvailable) || currentDurableState.equals(durableInUse)) {
                        inUseStatuses.add(new Infos.CandidateDurableSubStatus(newDurableSubStateID, subStateName, subStateDesc, availableForDurableFlag));
                    }
                } else if (CimStringUtils.equals(BizConstant.CIMFW_DURABLE_NOTAVAILABLE, durableState)) {
                    if (currentDurableState.equals(durableAvailable) || currentDurableState.equals(durableScrapped) || currentDurableState.equals(durableNotAvailable)) {
                        notAvailableStatuses.add(new Infos.CandidateDurableSubStatus(newDurableSubStateID, subStateName, subStateDesc, availableForDurableFlag));
                    }
                } else if (CimStringUtils.equals(BizConstant.CIMFW_DURABLE_SCRAPPED, durableState)) {
                    scrappedStatuses.add(new Infos.CandidateDurableSubStatus(newDurableSubStateID, subStateName, subStateDesc, availableForDurableFlag));
                }
            }
            if (!CimObjectUtils.isEmpty(availableStatuses)) {
                candidateDurableSubStatusDetails.add(new Infos.CandidateDurableSubStatusDetail(durableAvailable, availableStatuses));
            }
            if (!CimObjectUtils.isEmpty(inUseStatuses)) {
                candidateDurableSubStatusDetails.add(new Infos.CandidateDurableSubStatusDetail(durableInUse, inUseStatuses));
            }
            if (!CimObjectUtils.isEmpty(notAvailableStatuses)) {
                candidateDurableSubStatusDetails.add(new Infos.CandidateDurableSubStatusDetail(durableNotAvailable, notAvailableStatuses));
            }
            if (!CimObjectUtils.isEmpty(scrappedStatuses)) {
                candidateDurableSubStatusDetails.add(new Infos.CandidateDurableSubStatusDetail(durableScrapped, scrappedStatuses));
            }
        } else {
            for (int i = 0, j = 4; i < j; i++) {
                switch (i) {
                    case 0:
                        durableState = durableAvailable;
                        break;
                    case 1:
                        durableState = durableInUse;
                        break;
                    case 2:
                        durableState = durableNotAvailable;
                        break;
                    default:
                        durableState = durableScrapped;
                }
                if (durableState.equals(currentDurableState) || allInquiryFlag || changeToOtherFlag) {
                    if (!allInquiryFlag && !durableState.equals(currentDurableState)) {
                        if (durableState.equals(durableInUse)) {
                            if (!currentDurableState.equals(durableAvailable)) {
                                continue;
                            }
                        } else if (durableState.equals(durableNotAvailable)) {
                            if (!currentDurableState.equals(durableAvailable) && !currentDurableState.equals(durableScrapped)) {
                                continue;
                            }
                        }
                    }
                    if (CimBooleanUtils.isTrue(allInquiryFlag) || durableState.equals(currentDurableState)) {
                        CimDurableSubStateDO cimDurableSubStateExam = new CimDurableSubStateDO();
                        cimDurableSubStateExam.setDurableState(durableState);
                        List<CimDurableSubStateDO> dSubstateDOS = cimJpaRepository.findAll(Example.of(cimDurableSubStateExam));
                        List<Infos.CandidateDurableSubStatus> subStatuses = dSubstateDOS.stream()
                                .filter(data -> CimBooleanUtils.isTrue(allInquiryFlag) ||
                                        !ObjectIdentifier.equalsWithValue(ObjectIdentifier.build(data.getDurableSubStateID(), data.getId()), currentDurableSubStateID))
                                .map(data -> {
                                    Infos.CandidateDurableSubStatus subStatus = new Infos.CandidateDurableSubStatus();
                                    Boolean flag = data.getProcessAvailableFlag();
                                    subStatus.setAvailableForDurableFlag(flag);
                                    subStatus.setDurableSubStatus(ObjectIdentifier.build(data.getDurableSubStateID(), data.getId()));
                                    subStatus.setDurableSubStatusDescription(data.getDescription());
                                    return subStatus;
                                }).collect(Collectors.toList());
                        candidateDurableSubStatusDetails.add(new Infos.CandidateDurableSubStatusDetail(durableState, subStatuses));

                    } else {
                        CimDurableSubStateDO cimDurableSubStateExam = new CimDurableSubStateDO();
                        cimDurableSubStateExam.setDurableState(durableState);
                        cimDurableSubStateExam.setFromOtherFlag(true);
                        List<CimDurableSubStateDO> dSubstateDOS = cimJpaRepository.findAll(Example.of(cimDurableSubStateExam));
                        List<Infos.CandidateDurableSubStatus> subStatuses = dSubstateDOS.stream()
                                .filter(data -> CimBooleanUtils.isFalse(allInquiryFlag) &&
                                        !ObjectIdentifier.equalsWithValue(ObjectIdentifier.build(data.getDurableSubStateID(), data.getId()), currentDurableSubStateID))
                                .map(data -> {
                                    Infos.CandidateDurableSubStatus subStatus = new Infos.CandidateDurableSubStatus();
                                    Boolean flag = data.getProcessAvailableFlag();
                                    subStatus.setAvailableForDurableFlag(flag);
                                    subStatus.setDurableSubStatus(ObjectIdentifier.build(data.getDurableSubStateID(), data.getId()));
                                    subStatus.setDurableSubStatusDescription(data.getDescription());
                                    return subStatus;
                                }).collect(Collectors.toList());
                        candidateDurableSubStatusDetails.add(new Infos.CandidateDurableSubStatusDetail(durableState, subStatuses));
                    }
                }
            }
        }
        return candidateDurableSubStatusDetails;
    }

    @Override
    public void durableCurrentStateCheckTransition(Infos.ObjCommon objCommon, Inputs.ObjDurableCurrentStateCheckTransitionIn in) {
        log.info("ObjDurableCurrentStateCheckTransitionIn = {}", in);
        String durableCategory = in.getDurableCategory();
        ObjectIdentifier durableID = in.getDurableID();
        String durableStatus = in.getDurableStatus();
        ObjectIdentifier durableSubStatus = in.getDurableSubStatus();
        String currentDurableStatus = in.getCurrentDurableStatus();
        ObjectIdentifier currentDurableSubStatus = in.getCurrentDurableSubStatus();

        String currentDurableState = null;
        com.fa.cim.newcore.bo.durable.CimDurableSubState cimDurableSubState = null;
        CimCassette cimCassette = null;
        //---------------------------------
        //   Get Durable status,sub-status
        //---------------------------------
        log.info("Get Durable status,sub-status");
        if (SP_DURABLECAT_CASSETTE.equals(durableCategory)) {
            log.info("durableCategory is SP_DurableCat_Cassette");
            //---------------------------------
            //   Get Cassette Object
            //---------------------------------
            cimCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(null == cimCassette, new OmCode(retCodeConfig.getNotFoundCassette(), durableID.getValue()));
            currentDurableState = cimCassette.getDurableState();
            cimDurableSubState = cimCassette.getDurableSubState();
        } else if (SP_DURABLECAT_RETICLEPOD.equals(durableCategory)) {
            log.info("durableCategory is SP_DurableCat_ReticlePod");
            //---------------------------------
            //   Get ReticlePod Object
            //---------------------------------
            log.info("Get ReticlePod Object");
            CimReticlePod cimReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(null == cimReticlePod, new OmCode(retCodeConfig.getNotFoundReticlePod(), durableID.getValue()));
            currentDurableState = cimReticlePod.isAvailable() ? BizConstant.CIMFW_DURABLE_AVAILABLE
                    : (cimReticlePod.isInUse() ? BizConstant.CIMFW_DURABLE_INUSE
                    : (cimReticlePod.isNotAvailable() ? BizConstant.CIMFW_DURABLE_NOTAVAILABLE
                    : (cimReticlePod.isScrapped() ? BizConstant.CIMFW_DURABLE_SCRAPPED
                    : BizConstant.CIMFW_DURABLE_UNDEFINED)));

            cimDurableSubState = cimReticlePod.getDurableSubState();
        } else if (SP_DURABLECAT_RETICLE.equals(durableCategory)) {
            log.info("durableCategory is SP_DurableCat_Reticle");
            //---------------------------------
            //   Get Reticle Object
            //---------------------------------
            log.info("Get Reticle Object");
            CimProcessDurable cimProcessDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(null == cimProcessDurable, new OmCode(retCodeConfig.getNotFoundDurable(), durableID.getValue()));
            currentDurableState = cimProcessDurable.getDurableState();
            cimDurableSubState = cimProcessDurable.getDurableSubState();
        }
        ObjectIdentifier currentDurableSubStateID = (null == cimDurableSubState) ? null : new ObjectIdentifier(cimDurableSubState.getIdentifier(), cimDurableSubState.getPrimaryKey());
        log.info("currentDurableSubStateID = {}", currentDurableSubStateID);
        //--------------------------------------
        //   Check param's currentDurableStatus
        //--------------------------------------
        log.info("Check param's currentDurableStatus");
        if (!CimStringUtils.isEmpty(currentDurableStatus)) {
            Validations.check(!CimStringUtils.equals(currentDurableStatus, currentDurableState), new OmCode(retCodeConfig.getDurableStatChangedByOtherOperation(), ObjectIdentifier.fetchValue(durableID), currentDurableState));
        }
        //----------------------------------------
        //   Check param's currentDurableSubStatus
        //----------------------------------------
        log.info("Check param's currentDurableSubStatus");
        if (!ObjectIdentifier.isEmptyWithValue(currentDurableSubStatus)) {
            Validations.check(!ObjectIdentifier.equalsWithValue(currentDurableSubStateID, currentDurableSubStatus), new OmCode(retCodeConfig.getDurableStatChangedByOtherOperation(), ObjectIdentifier.fetchValue(durableID), ObjectIdentifier.fetchValue(currentDurableSubStateID)));
        }

        //--------------------------------------
        //   Check param's durableSubStatus
        //--------------------------------------
        log.info("Check param's durableSubStatus");
        if (ObjectIdentifier.isEmptyWithValue(durableSubStatus)) {
            log.info("input param's durableSubStatus is blank");
            if (null != cimDurableSubState) {
                Validations.check(true, retCodeConfig.getDurableNewSubstatBlank());
            } else {
                return;
            }
        }

        CimDurableSubState newCimDurableSubState1 = baseCoreFactory.getBO(CimDurableSubState.class, durableSubStatus);
        Validations.check(null == newCimDurableSubState1, retCodeConfig.getNotFoundDurableSubstat());
        if (null != cimDurableSubState) {
            log.info("durable's sub-status is not nil");
            Validations.check(ObjectIdentifier.equalsWithValue(currentDurableSubStateID, durableSubStatus), new OmCode(retCodeConfig.getSameDurableSubstat(), ObjectIdentifier.fetchValue(durableSubStatus)));
        }

        //--------------------------------------
        //   Check param's durableStatus
        //--------------------------------------
        String newDurableState = newCimDurableSubState1.getDurableState();

        if (!ObjectIdentifier.isEmptyWithValue(durableSubStatus)) {
            log.info("input param's durableStatus {}", durableStatus);
            Validations.check(!CimStringUtils.equals(durableStatus, newDurableState), new OmCode(retCodeConfig.getDurableStatSubstatUnmatch(), durableStatus, ObjectIdentifier.fetchValue(durableSubStatus)));
        }
        //--------------------------------------
        //   sort设备在做加工时出现异常将carrier状态改为NOTAVAILABLE.Sorter不去做Check next durableStatus
        //--------------------------------------
        if (!objCommon.getTransactionID().equals(TransactionIDEnum.SORT_ACTION_RPT.getValue())) {
            if (!CimStringUtils.equals(currentDurableState, newDurableState)) {
                log.info("current durable status is not equal new durable status");
                if (null != cimDurableSubState && !cimDurableSubState.isChangeToOtherDurableState()) {
                    log.error("objectCurrentDurableSubState->isChangeToOtherDurableState() is False");
                    Validations.check(true, retCodeConfig.getInvalidStateTransition());
                } else if (!newCimDurableSubState1.isChangeFromOtherDurableState()) {
                    log.error("newDurableSubState->isChangeFromOtherDurableState() is False");
                    Validations.check(true, retCodeConfig.getInvalidStateTransition());
                }
            }
            //--------------------------------------------------
            //   Get OM_DRBL_STATUS_CHG_LIMIT
            //--------------------------------------------------
            log.info("Get OM_DRBL_STATUS_CHG_LIMIT");
            int nTransitionLimit = StandardProperties.OM_DRBL_STATUS_CHG_LIMIT.getIntValue();
            //--------------------------------------------------
            //   Check next transition sub-state
            //--------------------------------------------------
            if (null != cimDurableSubState && (CimStringUtils.equals(currentDurableState, newDurableState) || nTransitionLimit == 1)) {
                log.info("Durable status do not change OR OM_DRBL_STATUS_CHG_LIMIT is 1");
                boolean checkFlag = false;
                List<ObjectIdentifier> nextTransitionSubStateSeq = cimDurableSubState.allDurableSubStateTransitions();
                if (!CimArrayUtils.isEmpty(nextTransitionSubStateSeq)) {
                    for (ObjectIdentifier nextTransitionSubState : nextTransitionSubStateSeq) {
                        if (ObjectIdentifier.equalsWithValue(durableSubStatus, nextTransitionSubState)) {
                            checkFlag = true;
                            break;
                        }
                    }
                    Validations.check(!checkFlag, retCodeConfigEx.getInvalidDurableStateTransition());
                }
            }
        }
        if (BizConstant.SP_DURABLE_CATEGORY_CASSETTE.equals(durableCategory)) {
            log.info("durableCategory is SP_DurableCat_Cassette");
            if (CimStringUtils.equals(BizConstant.CIMFW_DURABLE_SCRAPPED, newDurableState)) {
                Validations.check(!CimArrayUtils.isEmpty(cimCassette.allLots()), retCodeConfig.getCastHasAnyLots(), newDurableState);
            }
        }
    }

    @Override
    public void durableCurrentStateChange(Infos.ObjCommon objCommon, Inputs.ObjDurableCurrentStateChangeIn in) {
        log.info("ObjDurableCurrentStateChangeIn = {}", in);

        ObjectIdentifier userID = objCommon.getUser().getUserID();
        ObjectIdentifier durableID = in.getDurableID();
        CimPerson cimPerson = baseCoreFactory.getBO(CimPerson.class, userID);
        String durableCategory = in.getDurableCategory();
        String durableStatus = in.getDurableStatus();
        ObjectIdentifier durableSubStatus = in.getDurableSubStatus();
        if (ObjectIdentifier.isEmptyWithValue(durableSubStatus)) {
            log.info("param's durableSubStatus is blank");

            //--------------------------------------
            //   Change durable stauts
            //--------------------------------------
            log.info("Change durable stauts");
            if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE, durableCategory)) {
                cassetteMethod.cassetteStateChange(objCommon, durableID, durableStatus);
            } else if (CimStringUtils.equals(SP_DURABLECAT_RETICLE, durableCategory)) {
                reticleMethod.reticleStateChange(objCommon, durableID, durableStatus);
            } else if (CimStringUtils.equals(SP_DURABLECAT_RETICLEPOD, durableCategory)) {
                reticleMethod.reticlePodStatusChange(objCommon, durableStatus, durableID);
            }
        }
        //--------------------------------------
        //   Change durable sub stauts
        //--------------------------------------
        else {
            log.info("Change durable sub stauts");
            CimDurableSubState cimDurableSubState = baseCoreFactory.getBO(CimDurableSubState.class, durableSubStatus);
            Validations.check(null == cimDurableSubState, retCodeConfig.getNotFoundDurableSubstat());

            /*----------------------------------*/
            /*  Get Current Durable Sub-Status  */
            /*----------------------------------*/
            log.info("Get Current Durable Sub-Status");
            Infos.DurableSubStateGetOut durableSubStateGetOut = this.durableSubStateGet(objCommon, durableCategory, durableID);
            String currentDrblState = durableSubStateGetOut.getDurableStatus();
            if (!ObjectIdentifier.isEmptyWithValue(durableSubStatus)) {
                currentDrblState = currentDrblState + "." + ObjectIdentifier.fetchValue(durableSubStateGetOut.getDurableSubStatus());
            }
            String drblState = durableStatus + BizConstant.SP_KEY_SEPARATOR_DOT +
                    (!ObjectIdentifier.isEmpty(durableSubStatus) ? durableSubStatus.getValue() : BizConstant.EMPTY);

            if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE, durableCategory)) {
                CimCassette cimCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == cimCassette, retCodeConfig.getNotFoundCassette());

                /*--------------------------*/
                /*  Set Durable Sub-Status  */
                /*--------------------------*/
                try {
                    cimCassette.setDurableSubState(cimDurableSubState);
                } catch (CoreFrameworkException ex){
                    log.info("{}", "InvalidStateTransitionSignal exception");
                    Validations.check(true,
                            retCodeConfig.getInvalidStateTrans(),
                            currentDrblState, drblState);
                }
                cimCassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cimCassette.setLastClaimedPerson(cimPerson);
                cimCassette.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cimCassette.setStateChangedPerson(cimPerson);

            } else if (BizConstant.equalsIgnoreCase(SP_DURABLECAT_RETICLEPOD, durableCategory)) {
                log.info("durableCategory is SP_DurableCat_Reticle");
                CimReticlePod cimReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == cimReticlePod, retCodeConfig.getNotFoundReticlePod());

                /*--------------------------*/
                /*  Set Durable Sub-Status  */
                /*--------------------------*/

                cimReticlePod.setDurableSubState(cimDurableSubState);
                cimReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cimReticlePod.setLastClaimedPerson(cimPerson);
                cimReticlePod.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cimReticlePod.setStateChangedPerson(cimPerson);

            } else if (BizConstant.equalsIgnoreCase(SP_DURABLECAT_RETICLE, durableCategory)) {
                log.info("durableCategory is SP_DurableCat_ReticlePod");
                CimProcessDurable cimProcessDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == cimProcessDurable, retCodeConfig.getNotFoundReticle());

                /*--------------------------*/
                /*  Set Durable Sub-Status  */
                /*--------------------------*/
                cimProcessDurable.setDurableSubState(cimDurableSubState);
                cimProcessDurable.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cimProcessDurable.setLastClaimedPerson(cimPerson);
                cimProcessDurable.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cimProcessDurable.setStateChangedPerson(cimPerson);

                String reticleLocation = in.getReticleLocation();
                if (CimStringUtils.isNotEmpty(reticleLocation)){
                    reticleMethod.reticleLocationTrxUpdate(objCommon,cimProcessDurable,reticleLocation);
                }
                cimProcessDurable.setReticleLocation(reticleLocation);
            }
        }
    }

    @Override
    public List<Infos.StartDurable> durableControlJobDurableListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID) {
        CimDurableControlJobDO example = new CimDurableControlJobDO();
        example.setDurableControlJobID(ObjectIdentifier.fetchValue(durableControlJobID));
        List<CimDurableControlJobDurableDO> resultList = cimJpaRepository.findChildEntities(CimDurableControlJobDurableDO.class, Example.of(example));

        return CimArrayUtils.isEmpty(resultList) ? Collections.emptyList() : resultList.stream().map(data -> {
            Infos.StartDurable startDurable = new Infos.StartDurable();
            startDurable.setDurableId(ObjectIdentifier.build(data.getDurableID(), data.getDurableObj()));
            Infos.StartDurablePort startDurablePort = new Infos.StartDurablePort();
            startDurablePort.setLoadPortID(ObjectIdentifier.build(data.getLoadPortID(), data.getLoadPortObj()));
            startDurablePort.setUnloadPortID(ObjectIdentifier.build(data.getUnloadPortID(), data.getUnloadPortObj()));
            startDurablePort.setLoadSequenceNumber(data.getLoadSequenceNumber());
            startDurablePort.setLoadPurposeType(data.getLoadPurposeType());
            startDurable.setStartDurablePort(startDurablePort);
            return startDurable;
        }).collect(Collectors.toList());
    }


    @Override
    public String durableHoldStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        String durableHoldState = null;
        if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            durableHoldState = aCassette.getDurableHoldState();
        } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            durableHoldState = aReticlePod.getDurableHoldState();
        } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            durableHoldState = aReticle.getDurableHoldState();
        }
        return durableHoldState;
    }

    @Override
    public String durableProcessStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        String durableProcessState = null;
        if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            durableProcessState = aCassette.getDurableProcessState();
        } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            durableProcessState = aReticlePod.getDurableProcessState();
        } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            durableProcessState = aReticle.getDurableProcessState();
        }
        return durableProcessState;
    }

    @Override
    public void durableCheckEndBankIn(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        Boolean isBankInRequired = false;
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            isBankInRequired = aCassette.isBankInRequired();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            isBankInRequired = aReticlePod.isBankInRequired();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            isBankInRequired = aReticle.isBankInRequired();
        }

        if (isBankInRequired) {
            Validations.check(true, retCodeConfig.getBankinOperation());
        } else {
            Validations.check(true, retCodeConfig.getNotBankInOperation());
        }
    }

    @Override
    public Outputs.ObjDurableCurrentOperationInfoGetOut durableCurrentOperationInfoGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        Outputs.ObjDurableCurrentOperationInfoGetOut objDurableCurrentOperationInfoGetOut = new Outputs.ObjDurableCurrentOperationInfoGetOut();
        if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_CASSETTE)) {

            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);

            CimProcessDefinition aPosMainPD = aCassette.getMainProcessDefinition();
            // Get Durables' PO -> PD
            CimDurableProcessOperation aPosPO = aCassette.getDurableProcessOperation();
            Validations.check(null == aPosPO, new OmCode(retCodeConfig.getNotFoundDurablePo(), durableID.getValue()));

            CimProcessDefinition aPosPD = aPosPO.getProcessDefinition();

            String strOperNumber = aCassette.getOperationNumber();
            // Set output structure
            objDurableCurrentOperationInfoGetOut.setRouteID(new ObjectIdentifier(aPosMainPD.getIdentifier(), aPosMainPD.getPrimaryKey()));
            objDurableCurrentOperationInfoGetOut.setOperationID(new ObjectIdentifier(aPosPD.getIdentifier(), aPosPD.getPrimaryKey()));
            objDurableCurrentOperationInfoGetOut.setOperationNumber(strOperNumber);

        } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLEPOD)) {

            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);

            CimProcessDefinition aPosMainPD = aReticlePod.getMainProcessDefinition();
            // Get Durables' PO -> PD
            CimDurableProcessOperation aPosPO = aReticlePod.getDurableProcessOperation();
            Validations.check(null == aPosPO, new OmCode(retCodeConfig.getNotFoundDurablePo(), durableID.getValue()));

            CimProcessDefinition aPosPD = aPosPO.getProcessDefinition();
            String strOperNumber = aReticlePod.getOperationNumber();
            // Set output structure
            objDurableCurrentOperationInfoGetOut.setRouteID(new ObjectIdentifier(aPosMainPD.getIdentifier(), aPosMainPD.getPrimaryKey()));
            objDurableCurrentOperationInfoGetOut.setOperationID(new ObjectIdentifier(aPosPD.getIdentifier(), aPosPD.getPrimaryKey()));
            objDurableCurrentOperationInfoGetOut.setOperationNumber(strOperNumber);

        } else if (BizConstant.equalsIgnoreCase(durableCategory, SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            // Get Durables' Main PD
            CimProcessDefinition aPosMainPD = aReticle.getMainProcessDefinition();
            // Get Durables' PO -> PD
            CimDurableProcessOperation aPosPO = aReticle.getDurableProcessOperation();
            Validations.check(null == aPosPO, new OmCode(retCodeConfig.getNotFoundDurablePo(), durableID.getValue()));

            CimProcessDefinition aPosPD = aPosPO.getProcessDefinition();
            String strOperNumber = aReticle.getOperationNumber();

            objDurableCurrentOperationInfoGetOut.setRouteID(new ObjectIdentifier(aPosMainPD.getIdentifier(), aPosMainPD.getPrimaryKey()));
            objDurableCurrentOperationInfoGetOut.setOperationID(new ObjectIdentifier(aPosPD.getIdentifier(), aPosPD.getPrimaryKey()));
            objDurableCurrentOperationInfoGetOut.setOperationNumber(strOperNumber);
        }
        return objDurableCurrentOperationInfoGetOut;
    }

    @Override
    public Infos.DurableSubStatusInfo durableSubStateDBInfoGetDR(Infos.ObjCommon objCommon, Inputs.ObjDurableSubStateDBInfoGetDRIn in) {
        /*------------------*/
        /*    Initialize    */
        /*------------------*/
        CimDurableSubStateDO cimDurableSubStateExam = new CimDurableSubStateDO();
        cimDurableSubStateExam.setDurableSubStateID(in.getDurableSubStatus());
        return cimJpaRepository.findOne(Example.of(cimDurableSubStateExam)).map(drbSubState -> {
            // TODO: delete after TEST durableSubStateCore.findDistinctByDrblsubstID(in.getDurableSubStatus());
            Infos.DurableSubStatusInfo durableSubStatusInfo = new Infos.DurableSubStatusInfo();
            durableSubStatusInfo.setDurableSubStatus(drbSubState.getDurableSubStateID());
            durableSubStatusInfo.setDurableSubStatusDescription(drbSubState.getDescription());
            durableSubStatusInfo.setDurableStatus(drbSubState.getDurableState());
            durableSubStatusInfo.setAvailableForDurableFlag(drbSubState.getProcessAvailableFlag());
            durableSubStatusInfo.setChangeFromOtherFlag(drbSubState.getFromOtherFlag());
            durableSubStatusInfo.setChangeToOtherFlag(drbSubState.getToOtherFlag());
            durableSubStatusInfo.setConditionalAvailableFlag(drbSubState.getConditionAvailableLotFlag());

            if (in.getAvailableSubLotTypeInfoFlag()) {
                if (durableSubStatusInfo.getConditionalAvailableFlag()) {
                    CimDurableSubStateSltDO cimDurableSubStateSltExam = new CimDurableSubStateSltDO();
                    cimDurableSubStateSltExam.setReferenceKey(drbSubState.getId());
                    List<String> tepStr = cimJpaRepository.findAll(Example.of(cimDurableSubStateSltExam)).stream()
                            .map(CimDurableSubStateSltDO::getSubLotType)
                            .collect(Collectors.toList());
                    durableSubStatusInfo.setAvailableSubLotTypes(tepStr);
                }
            }
            if (in.getNextTransitionDurableSubStatusInfoFlag()) {
                CimDurableSubStateNextDO cimDurableSubStateNextExam = new CimDurableSubStateNextDO();
                cimDurableSubStateNextExam.setReferenceKey(drbSubState.getId());
                List<String> tepStr = cimJpaRepository.findAll(Example.of(cimDurableSubStateNextExam)).stream()
                        .map(CimDurableSubStateNextDO::getDurableSubStateID)
                        .collect(Collectors.toList());
                durableSubStatusInfo.setNextTransitionDurableSubStatus(tepStr);
            }
            return durableSubStatusInfo;
        }).orElse(null);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOperationHistoryFillInODRBQ020DRIn
     * @return java.util.List<com.fa.cim.dto.Infos.DurableOperationHisInfo>
     * @throws
     * @author ho
     * @date 2020/6/22 13:53
     */
    public List<Infos.DurableOperationHisInfo> durableOperationHistoryFillInODRBQ020DR(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOperationHistoryFillInODRBQ020DRIn strDurableOperationHistoryFillInODRBQ020DRIn) {
        List<Infos.DurableOperationHisInfo> strDurableOperationHistoryFillInODRBQ020DRout;
        log.info("PPTManager_i::durableOperationHistory_FillInODRBQ020DR");
        String tmpFHDRBLOPEHSCLAIM_USER_ID;

        log.info("in para durableCategory   = {}",
                strDurableOperationHistoryFillInODRBQ020DRIn.getDurableCategory());
        log.info("in para durableID         = {}",
                strDurableOperationHistoryFillInODRBQ020DRIn.getDurableID().getValue());
        log.info("in para routeID           = {}",
                strDurableOperationHistoryFillInODRBQ020DRIn.getRouteID().getValue());
        log.info("in para operationID       = {}",
                strDurableOperationHistoryFillInODRBQ020DRIn.getOperationID().getValue());
        log.info("in para operationNumber   = {}",
                strDurableOperationHistoryFillInODRBQ020DRIn.getOperationNumber());
        log.info("in para operationPass     = {}",
                strDurableOperationHistoryFillInODRBQ020DRIn.getOperationPass());
        log.info("in para operationCategory = {}",
                strDurableOperationHistoryFillInODRBQ020DRIn.getOperationCategory());
        log.info("in para pinPointFlag      = {}",
                (strDurableOperationHistoryFillInODRBQ020DRIn.getPinPointFlag() ? "TRUE" : "FALSE"));

        String lvhFHDRBLOPEHSDURABLE_ID = strDurableOperationHistoryFillInODRBQ020DRIn.getDurableID().getValue();
        String lvhFHDRBLOPEHSDRBL_CATEGORY = strDurableOperationHistoryFillInODRBQ020DRIn.getDurableCategory();
        String lvhFHDRBLOPEHSMAINPD_ID = strDurableOperationHistoryFillInODRBQ020DRIn.getRouteID().getValue();
        String lvhFHDRBLOPEHSOPE_NO = strDurableOperationHistoryFillInODRBQ020DRIn.getOperationNumber();
        String lvhFHDRBLOPEHSOPE_PASS_COUNT = strDurableOperationHistoryFillInODRBQ020DRIn.getOperationPass();
        String lvhFHDRBLOPEHSMOVE_TYPE = BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION;
        String lvhFHDRBLOPEHSMOVE_TYPE_1 = BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE;

        int lenOpeHisMax = StandardProperties.OM_OPEHIST_MAX_COUNT_FOR_INQ.getIntValue();
        log.info("OM_OPEHIST_MAX_COUNT_FOR_INQ {}", lenOpeHisMax);
        if (lenOpeHisMax < 100) {
            lenOpeHisMax = 100;
        }
        log.info("lenOpeHisMax {}", lenOpeHisMax);

        //-----------------------------------------------------------------------------
        //   1) If get durableID, RouteID/OperationNumber, pinPointFlag=TRUE
        //       ==> Get all types OpeHis of a specified operation
        //-----------------------------------------------------------------------------
        if (CimBooleanUtils.isTrue(strDurableOperationHistoryFillInODRBQ020DRIn.getPinPointFlag())) {
            log.info("pinPointFlag == TRUE");

            List<Object[]> cFHDRBLOPEHS1 = cimJpaRepository.query("SELECT DRBL_ID,\n" +
                            "                    DRBL_CATEGORY,\n" +
                            "                    PROCESS_ID,\n" +
                            "                    OPE_NO,\n" +
                            "                    STEP_ID,\n" +
                            "                    OPE_PASS_COUNT,\n" +
                            "                    PD_NAME,\n" +
                            "                    TRX_TIME,\n" +
                            "                    TRX_USER_ID,\n" +
                            "                    MOVE_TYPE,\n" +
                            "                    OPE_CATEGORY,\n" +
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
                            "                    RPARAM_COUNT,\n" +
                            "                    INITIAL_HOLD_FLAG,\n" +
                            "                    HOLD_TIME,\n" +
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
                            "                    PREV_PD_NAME,\n" +
                            "                    PREV_PASS_COUNT,\n" +
                            "                    PREV_STAGE_ID,\n" +
                            "                    PREV_STAGE_GRP_ID,\n" +
                            "                    PREV_PHOTO_LAYER,\n" +
                            "                    DCJ_ID,\n" +
                            "                    REWORK_COUNT,\n" +
                            "                    DRBL_OWNER_ID,\n" +
                            "                    PLAN_END_TIME,\n" +
                            "                    CRITERIA_FLAG,\n" +
                            "                    TRX_MEMO,\n" +
                            "                    STORE_TIME,\n" +
                            "                    RPARAM_CHG_TYPE,\n" +
                            "                    HOLD_OPE_NO,\n" +
                            "                    HOLD_REASON_OPE_NO\n" +
                            "            FROM OHDUROPE\n" +
                            "            WHERE DRBL_ID      = ?\n" +
                            "            AND DRBL_CATEGORY   = ?\n" +
                            "            AND ( (PROCESS_ID       = ?\n" +
                            "            AND OPE_NO          = ?\n" +
                            "            AND OPE_PASS_COUNT  = ?)\n" +
                            "            OR (PREV_PROCESS_ID  = ?\n" +
                            "            AND PREV_OPE_NO     = ?\n" +
                            "            AND PREV_PASS_COUNT = ?\n" +
                            "            AND (MOVE_TYPE = ? OR MOVE_TYPE = ?)))\n" +
                            "            ORDER BY TRX_TIME, EVENT_CREATE_TIME, STORE_TIME", lvhFHDRBLOPEHSDURABLE_ID
                    , lvhFHDRBLOPEHSDRBL_CATEGORY
                    , lvhFHDRBLOPEHSMAINPD_ID
                    , lvhFHDRBLOPEHSOPE_NO
                    , lvhFHDRBLOPEHSOPE_PASS_COUNT
                    , lvhFHDRBLOPEHSMAINPD_ID
                    , lvhFHDRBLOPEHSOPE_NO
                    , lvhFHDRBLOPEHSOPE_PASS_COUNT
                    , lvhFHDRBLOPEHSMOVE_TYPE
                    , lvhFHDRBLOPEHSMOVE_TYPE_1);

            int t_len1 = StandardProperties.OM_OPEHIST_EXTLEN_FOR_INQ_WITH_PIN_POINT.getIntValue();
            int count1 = 0;
            strDurableOperationHistoryFillInODRBQ020DRout = new ArrayList<>(t_len1);

            for (Object[] objects : cFHDRBLOPEHS1) {
                tmpFHDRBLOPEHSCLAIM_USER_ID = "";

                String hFHDRBLOPEHSDURABLE_ID = CimObjectUtils.toString(objects[0]);
                String hFHDRBLOPEHSDRBL_CATEGORY = CimObjectUtils.toString(objects[1]);
                String hFHDRBLOPEHSMAINPD_ID = CimObjectUtils.toString(objects[2]);
                String hFHDRBLOPEHSOPE_NO = CimObjectUtils.toString(objects[3]);
                String hFHDRBLOPEHSPD_ID = CimObjectUtils.toString(objects[4]);
                String hFHDRBLOPEHSOPE_PASS_COUNT = CimObjectUtils.toString(objects[5]);
                String hFHDRBLOPEHSPD_NAME = CimObjectUtils.toString(objects[6]);
                String hFHDRBLOPEHSCLAIM_TIME = CimObjectUtils.toString(objects[7]);
                tmpFHDRBLOPEHSCLAIM_USER_ID = CimObjectUtils.toString(objects[8]);
                String hFHDRBLOPEHSMOVE_TYPE = CimObjectUtils.toString(objects[9]);
                String hFHDRBLOPEHSOPE_CATEGORY = CimObjectUtils.toString(objects[10]);
                String hFHDRBLOPEHSSTAGE_ID = CimObjectUtils.toString(objects[11]);
                String hFHDRBLOPEHSSTAGEGRP_ID = CimObjectUtils.toString(objects[12]);
                String hFHDRBLOPEHSPHOTO_LAYER = CimObjectUtils.toString(objects[13]);
                String hFHDRBLOPEHSLOCATION_ID = CimObjectUtils.toString(objects[14]);
                String hFHDRBLOPEHSAREA_ID = CimObjectUtils.toString(objects[15]);
                String hFHDRBLOPEHSEQP_ID = CimObjectUtils.toString(objects[16]);
                String hFHDRBLOPEHSEQP_NAME = CimObjectUtils.toString(objects[17]);
                String hFHDRBLOPEHSOPE_MODE = CimObjectUtils.toString(objects[18]);
                String hFHDRBLOPEHSLC_RECIPE_ID = CimObjectUtils.toString(objects[19]);
                String hFHDRBLOPEHSRECIPE_ID = CimObjectUtils.toString(objects[20]);
                String hFHDRBLOPEHSPH_RECIPE_ID = CimObjectUtils.toString(objects[21]);
                String hFHDRBLOPEHSRPARM_COUNT = CimObjectUtils.toString(objects[22]);
                String hFHDRBLOPEHSINIT_HOLD_FLAG = CimObjectUtils.toString(objects[23]);
                String hFHDRBLOPEHSHOLD_TIME = CimObjectUtils.toString(objects[24]);
                String hFHDRBLOPEHSHOLD_USER_ID = CimObjectUtils.toString(objects[25]);
                String hFHDRBLOPEHSHOLD_TYPE = CimObjectUtils.toString(objects[26]);
                String hFHDRBLOPEHSHOLD_REASON_CODE = CimObjectUtils.toString(objects[27]);
                String hFHDRBLOPEHSHOLD_REASON_DESC = CimObjectUtils.toString(objects[28]);
                String hFHDRBLOPEHSREASON_CODE = CimObjectUtils.toString(objects[29]);
                String hFHDRBLOPEHSREASON_DESCRIPTION = CimObjectUtils.toString(objects[30]);
                String hFHDRBLOPEHSBANK_ID = CimObjectUtils.toString(objects[31]);
                String hFHDRBLOPEHSPREV_BANK_ID = CimObjectUtils.toString(objects[32]);
                String hFHDRBLOPEHSPREV_MAINPD_ID = CimObjectUtils.toString(objects[33]);
                String hFHDRBLOPEHSPREV_OPE_NO = CimObjectUtils.toString(objects[34]);
                String hFHDRBLOPEHSPREV_PD_ID = CimObjectUtils.toString(objects[35]);
                String hFHDRBLOPEHSPREV_PD_NAME = CimObjectUtils.toString(objects[36]);
                String hFHDRBLOPEHSPREV_PASS_COUNT = CimObjectUtils.toString(objects[37]);
                String hFHDRBLOPEHSPREV_STAGE_ID = CimObjectUtils.toString(objects[38]);
                String hFHDRBLOPEHSPREV_STAGEGRP_ID = CimObjectUtils.toString(objects[39]);
                String hFHDRBLOPEHSPREV_PHOTO_LAYER = CimObjectUtils.toString(objects[40]);
                String hFHDRBLOPEHSDCTRL_JOB = CimObjectUtils.toString(objects[41]);
                String hFHDRBLOPEHSREWORK_COUNT = CimObjectUtils.toString(objects[42]);
                String hFHDRBLOPEHSDRBL_OWNER_ID = CimObjectUtils.toString(objects[43]);
                String hFHDRBLOPEHSPLAN_END_TIME = CimObjectUtils.toString(objects[44]);
                String hFHDRBLOPEHSCRITERIA_FLAG = CimObjectUtils.toString(objects[45]);
                String hFHDRBLOPEHSCLAIM_MEMO = CimObjectUtils.toString(objects[46]);
                String hFHDRBLOPEHSSTORE_TIME = CimObjectUtils.toString(objects[47]);
                String hFHDRBLOPEHSRPARM_CHANGE_TYPE = CimObjectUtils.toString(objects[48]);
                String hFHDRBLOPEHSHOLD_OPE_NO = CimObjectUtils.toString(objects[49]);
                String hFHDRBLOPEHSHOLD_REASON_OPE_NO = CimObjectUtils.toString(objects[50]);

                if (count1 >= t_len1) {
                    t_len1 = t_len1 + StandardProperties.OM_OPEHIST_EXTLEN_FOR_INQ_WITH_PIN_POINT.getIntValue();
                }

                if (CimStringUtils.equals(hFHDRBLOPEHSMAINPD_ID,
                        strDurableOperationHistoryFillInODRBQ020DRIn.getRouteID().getValue()) &&
                        CimStringUtils.equals(hFHDRBLOPEHSPREV_OPE_NO,
                                strDurableOperationHistoryFillInODRBQ020DRIn.getOperationNumber()) &&
                        CimStringUtils.equals(hFHDRBLOPEHSPREV_PASS_COUNT, lvhFHDRBLOPEHSOPE_PASS_COUNT) &&
                        (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                                BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION) ||
                                CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                                        BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE))) {
                    log.info("Fetch prev. operation");

                    if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                            BizConstant.SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE) ||
                            CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                    BizConstant.SP_OPERATIONCATEGORY_OPERATIONCOMPLETE) ||
                            CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                    BizConstant.SP_OPERATIONCATEGORY_GATEPASS)) {

                        strDurableOperationHistoryFillInODRBQ020DRout
                                .add(new Infos.DurableOperationHisInfo());
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setRouteID(hFHDRBLOPEHSPREV_MAINPD_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setOperationNumber(hFHDRBLOPEHSPREV_OPE_NO);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setOperationPass(hFHDRBLOPEHSPREV_PASS_COUNT);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setOperationID(hFHDRBLOPEHSPREV_PD_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setOperationName(hFHDRBLOPEHSPREV_PD_NAME);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setStageID(hFHDRBLOPEHSPREV_STAGE_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setStageGroupID(hFHDRBLOPEHSPREV_STAGEGRP_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setMaskLevel(hFHDRBLOPEHSPREV_PHOTO_LAYER);
                    } else {
                        log.info("Fetch prev. operation - ignore record");
                        continue;
                    }
                } else {
                    if (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION) ||
                            CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                                    BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE)) {
                        log.info("Fetch backward movement record");

                        if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD) ||
                                CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                        BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) {
                            log.info("Fetch backward movement record - ignore record");
                            continue;
                        } else {
                            log.info("Fetch backward movement record - get record");

                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .add(new Infos.DurableOperationHisInfo());
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setRouteID(hFHDRBLOPEHSMAINPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationNumber(hFHDRBLOPEHSOPE_NO);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationID(hFHDRBLOPEHSPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationName(hFHDRBLOPEHSPD_NAME);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageID(hFHDRBLOPEHSSTAGE_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                        }
                    } else if (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION)
                            || CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE)) {
                        log.info("Fetch forward movement record");

                        if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)
                                || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) {
                            log.info("Fetch forward movement record - get record");

                            strDurableOperationHistoryFillInODRBQ020DRout.add(new Infos.DurableOperationHisInfo());
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setRouteID(hFHDRBLOPEHSMAINPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationNumber(hFHDRBLOPEHSOPE_NO);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationID(hFHDRBLOPEHSPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationName(hFHDRBLOPEHSPD_NAME);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setStageID(hFHDRBLOPEHSSTAGE_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                        } else {
                            log.info("Fetch forward movement record - ignore record");
                            continue;
                        }
                    } else {

                        log.info("Fetch non movement record - get record");

                        strDurableOperationHistoryFillInODRBQ020DRout.add(new Infos.DurableOperationHisInfo());
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setRouteID(hFHDRBLOPEHSMAINPD_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setOperationNumber(hFHDRBLOPEHSOPE_NO);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setOperationID(hFHDRBLOPEHSPD_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setOperationName(hFHDRBLOPEHSPD_NAME);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setStageID(hFHDRBLOPEHSSTAGE_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                    }
                }

                log.info("set other data members");

                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setReportTimeStamp(hFHDRBLOPEHSCLAIM_TIME);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setOperationCategory(hFHDRBLOPEHSOPE_CATEGORY);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setLocationID(hFHDRBLOPEHSLOCATION_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setWorkArea(hFHDRBLOPEHSAREA_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setEquipmentID(hFHDRBLOPEHSEQP_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setEquipmentName(hFHDRBLOPEHSEQP_NAME);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setOperationMode(hFHDRBLOPEHSOPE_MODE);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setLogicalRecipeID(hFHDRBLOPEHSLC_RECIPE_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setMachineRecipeID(hFHDRBLOPEHSRECIPE_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setPhysicalRecipeID(hFHDRBLOPEHSPH_RECIPE_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setDurableControlJobID(hFHDRBLOPEHSDCTRL_JOB);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setReworkCount(CimLongUtils.longValue(hFHDRBLOPEHSREWORK_COUNT));
                String buffer1;
                buffer1 = hFHDRBLOPEHSINIT_HOLD_FLAG;
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setInitialHoldFlag(buffer1);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setHoldType(hFHDRBLOPEHSHOLD_TYPE);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setHoldTimeStamp(hFHDRBLOPEHSHOLD_TIME);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setHoldUserID(hFHDRBLOPEHSHOLD_USER_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setHoldReasonCodeID(hFHDRBLOPEHSHOLD_REASON_CODE);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setHoldReasonCodeDescription(hFHDRBLOPEHSHOLD_REASON_DESC);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setReasonCodeID(hFHDRBLOPEHSREASON_CODE);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setReasonCodeDescription(hFHDRBLOPEHSREASON_DESCRIPTION);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setBankID(hFHDRBLOPEHSBANK_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setPreviousBankID(hFHDRBLOPEHSPREV_BANK_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setDurableOwnerID(hFHDRBLOPEHSDRBL_OWNER_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setDueTimeStamp(hFHDRBLOPEHSPLAN_END_TIME);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setTestCriteriaFlag(CimBooleanUtils.isTrue(hFHDRBLOPEHSCRITERIA_FLAG));
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setUserID(tmpFHDRBLOPEHSCLAIM_USER_ID);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setClaimMemo(hFHDRBLOPEHSCLAIM_MEMO);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setStoreTimeStamp(hFHDRBLOPEHSSTORE_TIME);
                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setRecipeParameterChangeType(hFHDRBLOPEHSRPARM_CHANGE_TYPE);
                log.info("recipeParameterChangeType {}", strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .getRecipeParameterChangeType());

                List<Infos.OpeHisRecipeParmInfo> strDurableOpeHisRParmGetDROut;
                Infos.DurableOpeHisRParmGetDRIn strDurableOpeHisRParmGetDRin = new Infos.DurableOpeHisRParmGetDRIn();
                strDurableOpeHisRParmGetDRin.setDurableCategory(hFHDRBLOPEHSDRBL_CATEGORY);
                strDurableOpeHisRParmGetDRin.setDurableID(ObjectIdentifier.buildWithValue(hFHDRBLOPEHSDURABLE_ID));
                strDurableOpeHisRParmGetDRin.setRouteID(hFHDRBLOPEHSMAINPD_ID);
                strDurableOpeHisRParmGetDRin.setOperationNumber(hFHDRBLOPEHSOPE_NO);
                strDurableOpeHisRParmGetDRin
                        .setOperationPassCount(CimLongUtils.longValue(hFHDRBLOPEHSOPE_PASS_COUNT));
                strDurableOpeHisRParmGetDRin.setClaimTime(hFHDRBLOPEHSCLAIM_TIME);
                strDurableOpeHisRParmGetDRin.setOperationCategory(hFHDRBLOPEHSOPE_CATEGORY);
                strDurableOpeHisRParmGetDROut = durableOpeHisRParmGetDR(strObjCommonIn,
                        strDurableOpeHisRParmGetDRin);

                strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                        .setStrOpeHisRecipeParmInfo(strDurableOpeHisRParmGetDROut);

                count1++;
                if (count1 >= lenOpeHisMax) {
                    log.info(" Length of OpeHis reached the maximum {}", count1);
                    break;
                }
            }
        } else {
            log.info("pinPointFlag == FALSE");

            List<Object[]> cFHDRBLOPEHS2 = cimJpaRepository.query("SELECT PROCESS_ID,\n" +
                            "                    OPE_NO,\n" +
                            "                    OPE_PASS_COUNT,\n" +
                            "                    TRX_TIME,\n" +
                            "                    MOVE_TYPE,\n" +
                            "                    OPE_CATEGORY,\n" +
                            "                    PREV_PROCESS_ID,\n" +
                            "                    PREV_OPE_NO,\n" +
                            "                    PREV_PASS_COUNT\n" +
                            "            FROM OHDUROPE\n" +
                            "            WHERE DRBL_ID      = ?\n" +
                            "            AND DRBL_CATEGORY   = ?\n" +
                            "            AND ( (PROCESS_ID       = ?\n" +
                            "            AND OPE_NO          = ?\n" +
                            "            AND OPE_PASS_COUNT  = ?)\n" +
                            "            OR (PREV_PROCESS_ID  = ?\n" +
                            "            AND PREV_OPE_NO     = ?\n" +
                            "            AND PREV_PASS_COUNT = ?\n" +
                            "            AND (MOVE_TYPE = ? OR MOVE_TYPE = ?)))\n" +
                            "            ORDER BY TRX_TIME, EVENT_CREATE_TIME, STORE_TIME", lvhFHDRBLOPEHSDURABLE_ID
                    , lvhFHDRBLOPEHSDRBL_CATEGORY
                    , lvhFHDRBLOPEHSMAINPD_ID
                    , lvhFHDRBLOPEHSOPE_NO
                    , lvhFHDRBLOPEHSOPE_PASS_COUNT
                    , lvhFHDRBLOPEHSMAINPD_ID
                    , lvhFHDRBLOPEHSOPE_NO
                    , lvhFHDRBLOPEHSOPE_PASS_COUNT
                    , lvhFHDRBLOPEHSMOVE_TYPE
                    , lvhFHDRBLOPEHSMOVE_TYPE_1);

            boolean foundFlag = false;

            String lvhFHDRBLOPEHSCLAIM_TIME = null;
            for (Object[] objects : cFHDRBLOPEHS2) {
                String hFHDRBLOPEHSMAINPD_ID = CimObjectUtils.toString(objects[0]);
                String hFHDRBLOPEHSOPE_NO = CimObjectUtils.toString(objects[1]);
                String hFHDRBLOPEHSOPE_PASS_COUNT = CimObjectUtils.toString(objects[2]);
                lvhFHDRBLOPEHSCLAIM_TIME = CimObjectUtils.toString(objects[3]);
                String hFHDRBLOPEHSMOVE_TYPE = CimObjectUtils.toString(objects[4]);
                String hFHDRBLOPEHSOPE_CATEGORY = CimObjectUtils.toString(objects[5]);
                String hFHDRBLOPEHSPREV_MAINPD_ID = CimObjectUtils.toString(objects[6]);
                String hFHDRBLOPEHSPREV_OPE_NO = CimObjectUtils.toString(objects[7]);
                String hFHDRBLOPEHSPREV_PASS_COUNT = CimObjectUtils.toString(objects[8]);

                if (CimStringUtils.length(strDurableOperationHistoryFillInODRBQ020DRIn.getOperationCategory()) != 0
                        && !CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                        strDurableOperationHistoryFillInODRBQ020DRIn.getOperationCategory())) {
                    log.info("{}", "Fetch specified operation category (cFHDRBLOPEHS2) - ignore record ");
                    continue;

                }

                if (CimStringUtils.equals(hFHDRBLOPEHSPREV_MAINPD_ID,
                        strDurableOperationHistoryFillInODRBQ020DRIn.getRouteID().getValue())
                        && CimStringUtils.equals(hFHDRBLOPEHSPREV_OPE_NO,
                        strDurableOperationHistoryFillInODRBQ020DRIn.getOperationNumber())
                        && CimStringUtils.equals(hFHDRBLOPEHSPREV_PASS_COUNT, lvhFHDRBLOPEHSOPE_PASS_COUNT) &&
                        (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                                BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION)
                                || CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                                BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE))) {
                    log.info("{}", "Fetch prev. operation - cFHDRBLOPEHS2");

                    if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                            BizConstant.SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE)
                            || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                            BizConstant.SP_OPERATIONCATEGORY_OPERATIONCOMPLETE)
                            || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                            BizConstant.SP_OPERATIONCATEGORY_GATEPASS)) {
                        log.info("{}", "Fetch prev. operation (cFHDRBLOPEHS2) - get record");
                        foundFlag = true;
                        break;
                    } else {
                        log.info("{}", "Fetch prev. operation  (cFHDRBLOPEHS2) - ignore record");
                        continue;
                    }
                } else {
                    if (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION)
                            || CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE)) {
                        log.info("{}", "Fetch backward movement record (cFHDRBLOPEHS2) ");

                        if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)
                                || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) {
                            log.info("{}", "Fetch backward movement record (cFHDRBLOPEHS2) - ignore record");
                            continue;
                        } else {
                            log.info("{}", "Fetch backward movement record (cFHDRBLOPEHS2) - get record");
                            foundFlag = true;
                            break;
                        }
                    } else if (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION)
                            || CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE)) {
                        log.info("{}", "Fetch forward movement record (cFHDRBLOPEHS2) ");

                        if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)
                                || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) {
                            log.info("{}", "Fetch forward movement record (cFHDRBLOPEHS2) - get record");
                            foundFlag = true;
                            break;
                        } else {
                            log.info("{}", "Fetch forward movement record (cFHDRBLOPEHS2) - ignore record");
                            continue;
                        }
                    } else {
                        log.info("{}", "Fetch non movement record (cFHDRBLOPEHS2) - get record");
                        foundFlag = true;
                        break;
                    }
                }
            }

            if (!foundFlag) {
                log.info("{}", "Not found record (cFHDRBLOPEHS2) ");
                log.info("PPTManager_i::durableOperationHistory_FillInODRBQ020DR");
                return new ArrayList<>(0);
            }

            //-----------------------------------------------------------------------------
            //   2) If get durableID, RouteID/OperationNumber, category, pinPointFlag=FALSE
            //       ==> Get specified types OpeHis of all operations after
            //           specified point( = RouteID/OperationNumber).
            //-----------------------------------------------------------------------------
            if (CimStringUtils.length(strDurableOperationHistoryFillInODRBQ020DRIn.getOperationCategory()) != 0) {
                log.info("{}", "CIMFWStrLen(operationCategory) != 0");

                String hFHDRBLOPEHSOPE_CATEGORY = strDurableOperationHistoryFillInODRBQ020DRIn.getOperationCategory();

                //--- Get all appropriate data and Set tx output data
                List<Object[]> cFHDRBLOPEHS3 = cimJpaRepository.query("SELECT DRBL_ID,\n" +
                                "                        DRBL_CATEGORY,\n" +
                                "                        PROCESS_ID,\n" +
                                "                        OPE_NO,\n" +
                                "                        STEP_ID,\n" +
                                "                        OPE_PASS_COUNT,\n" +
                                "                        PD_NAME,\n" +
                                "                        TRX_TIME,\n" +
                                "                        TRX_USER_ID,\n" +
                                "                        MOVE_TYPE,\n" +
                                "                        OPE_CATEGORY,\n" +
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
                                "                        RPARAM_COUNT,\n" +
                                "                        INITIAL_HOLD_FLAG,\n" +
                                "                        HOLD_TIME,\n" +
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
                                "                        PREV_PD_NAME,\n" +
                                "                        PREV_PASS_COUNT,\n" +
                                "                        PREV_STAGE_ID,\n" +
                                "                        PREV_STAGE_GRP_ID,\n" +
                                "                        PREV_PHOTO_LAYER,\n" +
                                "                        DCJ_ID,\n" +
                                "                        REWORK_COUNT,\n" +
                                "                        DRBL_OWNER_ID,\n" +
                                "                        PLAN_END_TIME,\n" +
                                "                        CRITERIA_FLAG,\n" +
                                "                        TRX_MEMO,\n" +
                                "                        STORE_TIME,\n" +
                                "                        RPARAM_CHG_TYPE,\n" +
                                "                        HOLD_OPE_NO,\n" +
                                "                        HOLD_REASON_OPE_NO\n" +
                                "                FROM OHDUROPE\n" +
                                "                WHERE DRBL_ID      = ?\n" +
                                "                AND DRBL_CATEGORY   = ?\n" +
                                "                AND OPE_CATEGORY    = ?\n" +
                                "                AND CLAIM_TIME     >= ?\n" +
                                "                ORDER BY TRX_TIME, EVENT_CREATE_TIME, STORE_TIME",
                        lvhFHDRBLOPEHSDURABLE_ID
                        , lvhFHDRBLOPEHSDRBL_CATEGORY
                        , hFHDRBLOPEHSOPE_CATEGORY
                        , lvhFHDRBLOPEHSCLAIM_TIME);

                int t_len1 = StandardProperties.OM_OPEHIST_EXTLEN_FOR_INQ.getIntValue();
                int count1 = 0;
                strDurableOperationHistoryFillInODRBQ020DRout = new ArrayList<>(t_len1);

                for (Object[] objects : cFHDRBLOPEHS3) {
                    tmpFHDRBLOPEHSCLAIM_USER_ID = "";

                    String hFHDRBLOPEHSDURABLE_ID = CimObjectUtils.toString(objects[0]);
                    String hFHDRBLOPEHSDRBL_CATEGORY = CimObjectUtils.toString(objects[1]);
                    String hFHDRBLOPEHSMAINPD_ID = CimObjectUtils.toString(objects[2]);
                    String hFHDRBLOPEHSOPE_NO = CimObjectUtils.toString(objects[3]);
                    String hFHDRBLOPEHSPD_ID = CimObjectUtils.toString(objects[4]);
                    String hFHDRBLOPEHSOPE_PASS_COUNT = CimObjectUtils.toString(objects[5]);
                    String hFHDRBLOPEHSPD_NAME = CimObjectUtils.toString(objects[6]);
                    String hFHDRBLOPEHSCLAIM_TIME = CimObjectUtils.toString(objects[7]);
                    tmpFHDRBLOPEHSCLAIM_USER_ID = CimObjectUtils.toString(objects[8]);
                    String hFHDRBLOPEHSMOVE_TYPE = CimObjectUtils.toString(objects[9]);
                    hFHDRBLOPEHSOPE_CATEGORY = CimObjectUtils.toString(objects[10]);
                    String hFHDRBLOPEHSSTAGE_ID = CimObjectUtils.toString(objects[11]);
                    String hFHDRBLOPEHSSTAGEGRP_ID = CimObjectUtils.toString(objects[12]);
                    String hFHDRBLOPEHSPHOTO_LAYER = CimObjectUtils.toString(objects[13]);
                    String hFHDRBLOPEHSLOCATION_ID = CimObjectUtils.toString(objects[14]);
                    String hFHDRBLOPEHSAREA_ID = CimObjectUtils.toString(objects[15]);
                    String hFHDRBLOPEHSEQP_ID = CimObjectUtils.toString(objects[16]);
                    String hFHDRBLOPEHSEQP_NAME = CimObjectUtils.toString(objects[17]);
                    String hFHDRBLOPEHSOPE_MODE = CimObjectUtils.toString(objects[18]);
                    String hFHDRBLOPEHSLC_RECIPE_ID = CimObjectUtils.toString(objects[19]);
                    String hFHDRBLOPEHSRECIPE_ID = CimObjectUtils.toString(objects[20]);
                    String hFHDRBLOPEHSPH_RECIPE_ID = CimObjectUtils.toString(objects[21]);
                    String hFHDRBLOPEHSRPARM_COUNT = CimObjectUtils.toString(objects[22]);
                    String hFHDRBLOPEHSINIT_HOLD_FLAG = CimObjectUtils.toString(objects[23]);
                    String hFHDRBLOPEHSHOLD_TIME = CimObjectUtils.toString(objects[24]);
                    String hFHDRBLOPEHSHOLD_USER_ID = CimObjectUtils.toString(objects[25]);
                    String hFHDRBLOPEHSHOLD_TYPE = CimObjectUtils.toString(objects[26]);
                    String hFHDRBLOPEHSHOLD_REASON_CODE = CimObjectUtils.toString(objects[27]);
                    String hFHDRBLOPEHSHOLD_REASON_DESC = CimObjectUtils.toString(objects[28]);
                    String hFHDRBLOPEHSREASON_CODE = CimObjectUtils.toString(objects[29]);
                    String hFHDRBLOPEHSREASON_DESCRIPTION = CimObjectUtils.toString(objects[30]);
                    String hFHDRBLOPEHSBANK_ID = CimObjectUtils.toString(objects[31]);
                    String hFHDRBLOPEHSPREV_BANK_ID = CimObjectUtils.toString(objects[32]);
                    String hFHDRBLOPEHSPREV_MAINPD_ID = CimObjectUtils.toString(objects[33]);
                    String hFHDRBLOPEHSPREV_OPE_NO = CimObjectUtils.toString(objects[34]);
                    String hFHDRBLOPEHSPREV_PD_ID = CimObjectUtils.toString(objects[35]);
                    String hFHDRBLOPEHSPREV_PD_NAME = CimObjectUtils.toString(objects[36]);
                    String hFHDRBLOPEHSPREV_PASS_COUNT = CimObjectUtils.toString(objects[37]);
                    String hFHDRBLOPEHSPREV_STAGE_ID = CimObjectUtils.toString(objects[38]);
                    String hFHDRBLOPEHSPREV_STAGEGRP_ID = CimObjectUtils.toString(objects[39]);
                    String hFHDRBLOPEHSPREV_PHOTO_LAYER = CimObjectUtils.toString(objects[40]);
                    String hFHDRBLOPEHSDCTRL_JOB = CimObjectUtils.toString(objects[41]);
                    String hFHDRBLOPEHSREWORK_COUNT = CimObjectUtils.toString(objects[42]);
                    String hFHDRBLOPEHSDRBL_OWNER_ID = CimObjectUtils.toString(objects[43]);
                    String hFHDRBLOPEHSPLAN_END_TIME = CimObjectUtils.toString(objects[44]);
                    String hFHDRBLOPEHSCRITERIA_FLAG = CimObjectUtils.toString(objects[45]);
                    String hFHDRBLOPEHSCLAIM_MEMO = CimObjectUtils.toString(objects[46]);
                    String hFHDRBLOPEHSSTORE_TIME = CimObjectUtils.toString(objects[47]);
                    String hFHDRBLOPEHSRPARM_CHANGE_TYPE = CimObjectUtils.toString(objects[48]);
                    String hFHDRBLOPEHSHOLD_OPE_NO = CimObjectUtils.toString(objects[49]);
                    String hFHDRBLOPEHSHOLD_REASON_OPE_NO = CimObjectUtils.toString(objects[50]);

                    if (count1 >= t_len1) {
                        t_len1 = t_len1 + StandardProperties.OM_OPEHIST_EXTLEN_FOR_INQ.getIntValue();
                    }

                    if (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION)
                            || CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE)) {
                        log.info("{}", "Fetch backward movement record");

                        if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)
                                || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) {
                            log.info("{}", "Fetch backward movement record - ignore record");
                            continue;
                        } else {
                            log.info("{}", "Fetch backward movement record - get record");

                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .add(new Infos.DurableOperationHisInfo());
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setRouteID(hFHDRBLOPEHSMAINPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationNumber(hFHDRBLOPEHSOPE_NO);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationID(hFHDRBLOPEHSPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationName(hFHDRBLOPEHSPD_NAME);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageID(hFHDRBLOPEHSSTAGE_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                        }
                    } else if (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION)
                            || CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE)) {
                        log.info("{}", "Fetch forward movement record");

                        if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE)
                                || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_OPERATIONCOMPLETE)
                                || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_GATEPASS)) {
                            log.info("{}", "Fetch forward movement record (1)- get record");

                            strDurableOperationHistoryFillInODRBQ020DRout.add(new Infos.DurableOperationHisInfo());
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setRouteID(hFHDRBLOPEHSPREV_MAINPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationNumber(hFHDRBLOPEHSPREV_OPE_NO);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationPass(hFHDRBLOPEHSPREV_PASS_COUNT);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationID(hFHDRBLOPEHSPREV_PD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationName(hFHDRBLOPEHSPREV_PD_NAME);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setStageID(hFHDRBLOPEHSPREV_STAGE_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setStageGroupID(hFHDRBLOPEHSPREV_STAGEGRP_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setMaskLevel(hFHDRBLOPEHSPREV_PHOTO_LAYER);
                        } else if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)
                                || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) {
                            log.info("{}", "Fetch forward movement record (2)- get record");

                            strDurableOperationHistoryFillInODRBQ020DRout.add(new Infos.DurableOperationHisInfo());
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setRouteID(hFHDRBLOPEHSMAINPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationNumber(hFHDRBLOPEHSOPE_NO);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationID(hFHDRBLOPEHSPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setOperationName(hFHDRBLOPEHSPD_NAME);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setStageID(hFHDRBLOPEHSSTAGE_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                    .setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                        } else {
                            log.info("{}", "Fetch forward movement record - ignore record");
                            continue;
                        }
                    } else {
                        log.info("{}", "Fetch non movement record - get record");

                        strDurableOperationHistoryFillInODRBQ020DRout.add(new Infos.DurableOperationHisInfo());
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setRouteID(hFHDRBLOPEHSMAINPD_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setOperationNumber(hFHDRBLOPEHSOPE_NO);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setOperationID(hFHDRBLOPEHSPD_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setOperationName(hFHDRBLOPEHSPD_NAME);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setStageID(hFHDRBLOPEHSSTAGE_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                                .setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                    }

                    log.info("{}", "set other data members");

                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setReportTimeStamp(hFHDRBLOPEHSCLAIM_TIME);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setOperationCategory(hFHDRBLOPEHSOPE_CATEGORY);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setLocationID(hFHDRBLOPEHSLOCATION_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setWorkArea(hFHDRBLOPEHSAREA_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setEquipmentID(hFHDRBLOPEHSEQP_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setEquipmentName(hFHDRBLOPEHSEQP_NAME);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setOperationMode(hFHDRBLOPEHSOPE_MODE);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setLogicalRecipeID(hFHDRBLOPEHSLC_RECIPE_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setMachineRecipeID(hFHDRBLOPEHSRECIPE_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setPhysicalRecipeID(hFHDRBLOPEHSPH_RECIPE_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setDurableControlJobID(hFHDRBLOPEHSDCTRL_JOB);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setReworkCount(CimLongUtils.longValue(hFHDRBLOPEHSREWORK_COUNT));
                    String buffer1;
                    buffer1 = hFHDRBLOPEHSINIT_HOLD_FLAG;
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setInitialHoldFlag(buffer1);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setHoldType(hFHDRBLOPEHSHOLD_TYPE);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setHoldTimeStamp(hFHDRBLOPEHSHOLD_TIME);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setHoldUserID(hFHDRBLOPEHSHOLD_USER_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setHoldReasonCodeID(hFHDRBLOPEHSHOLD_REASON_CODE);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setHoldReasonCodeDescription(hFHDRBLOPEHSHOLD_REASON_DESC);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setReasonCodeID(hFHDRBLOPEHSREASON_CODE);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setReasonCodeDescription(hFHDRBLOPEHSREASON_DESCRIPTION);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setBankID(hFHDRBLOPEHSBANK_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setPreviousBankID(hFHDRBLOPEHSPREV_BANK_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setDurableOwnerID(hFHDRBLOPEHSDRBL_OWNER_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setDueTimeStamp(hFHDRBLOPEHSPLAN_END_TIME);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setTestCriteriaFlag(CimBooleanUtils.isTrue(hFHDRBLOPEHSCRITERIA_FLAG));
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setUserID(tmpFHDRBLOPEHSCLAIM_USER_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setClaimMemo(hFHDRBLOPEHSCLAIM_MEMO);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setStoreTimeStamp(hFHDRBLOPEHSSTORE_TIME);
                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setRecipeParameterChangeType(hFHDRBLOPEHSRPARM_CHANGE_TYPE);

                    // (*3)  set pptOperationHisInfo[i].strOpeHisRecipeParmInfo
                    log.info("{}", "call durable_opeHisRParm_GetDR()");
                    List<Infos.OpeHisRecipeParmInfo> strDurableOpeHisRParmGetDROut;
                    Infos.DurableOpeHisRParmGetDRIn strDurableOpeHisRParmGetDRin = new Infos
                            .DurableOpeHisRParmGetDRIn();
                    strDurableOpeHisRParmGetDRin.setDurableCategory(hFHDRBLOPEHSDRBL_CATEGORY);
                    strDurableOpeHisRParmGetDRin
                            .setDurableID(ObjectIdentifier.buildWithValue(hFHDRBLOPEHSDURABLE_ID));
                    strDurableOpeHisRParmGetDRin.setRouteID(hFHDRBLOPEHSMAINPD_ID);
                    strDurableOpeHisRParmGetDRin.setOperationNumber(hFHDRBLOPEHSOPE_NO);
                    strDurableOpeHisRParmGetDRin
                            .setOperationPassCount(CimLongUtils.longValue(hFHDRBLOPEHSOPE_PASS_COUNT));
                    strDurableOpeHisRParmGetDRin.setClaimTime(hFHDRBLOPEHSCLAIM_TIME);
                    strDurableOpeHisRParmGetDRin.setOperationCategory(hFHDRBLOPEHSOPE_CATEGORY);
                    strDurableOpeHisRParmGetDROut = durableOpeHisRParmGetDR(strObjCommonIn,
                            strDurableOpeHisRParmGetDRin);

                    strDurableOperationHistoryFillInODRBQ020DRout.get(count1)
                            .setStrOpeHisRecipeParmInfo(strDurableOpeHisRParmGetDROut);
                    count1++;

                    if (count1 >= lenOpeHisMax) {
                        log.info("{} {}", " Length of OpeHis reached the maximum ", count1);
                        break;
                    }
                }
            } else {
                //-----------------------------------------------------------------------------
                //   3) If get lotID, RouteID/OperationNumber, pinPointFlag=FALSE
                //       ==> Get all types OpeHis of all operations after
                //           specified point.
                //-----------------------------------------------------------------------------
                log.info("{}", "CIMFWStrLen(operationCategory) == 0");

                //--- Get all appropriate data and Set tx output data
                List<Object[]> cFHDRBLOPEHS4 = cimJpaRepository.query("SELECT DRBL_ID,\n" +
                                "                        DRBL_CATEGORY,\n" +
                                "                        PROCESS_ID,\n" +
                                "                        OPE_NO,\n" +
                                "                        STEP_ID,\n" +
                                "                        OPE_PASS_COUNT,\n" +
                                "                        PD_NAME,\n" +
                                "                        TRX_TIME,\n" +
                                "                        TRX_USER_ID,\n" +
                                "                        MOVE_TYPE,\n" +
                                "                        OPE_CATEGORY,\n" +
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
                                "                        RPARAM_COUNT,\n" +
                                "                        INITIAL_HOLD_FLAG,\n" +
                                "                        HOLD_TIME,\n" +
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
                                "                        PREV_PD_NAME,\n" +
                                "                        PREV_PASS_COUNT,\n" +
                                "                        PREV_STAGE_ID,\n" +
                                "                        PREV_STAGE_GRP_ID,\n" +
                                "                        PREV_PHOTO_LAYER,\n" +
                                "                        DCJ_ID,\n" +
                                "                        REWORK_COUNT,\n" +
                                "                        DRBL_OWNER_ID,\n" +
                                "                        PLAN_END_TIME,\n" +
                                "                        CRITERIA_FLAG,\n" +
                                "                        TRX_MEMO,\n" +
                                "                        STORE_TIME,\n" +
                                "                        RPARAM_CHG_TYPE,\n" +
                                "                        HOLD_OPE_NO,\n" +
                                "                        HOLD_REASON_OPE_NO\n" +
                                "                FROM OHDUROPE\n" +
                                "                WHERE DRBL      = ?\n" +
                                "                AND DRBL_CATEGORY   = ?\n" +
                                "                AND TRX_TIME, EVENT_CREATE_TIME, STORE_TIME", lvhFHDRBLOPEHSDURABLE_ID
                        , lvhFHDRBLOPEHSDRBL_CATEGORY
                        , lvhFHDRBLOPEHSCLAIM_TIME);

                log.info("{} {}", "lvhFHDRBLOPEHSDURABLE_ID =", lvhFHDRBLOPEHSDURABLE_ID);
                log.info("{} {}", "lvhFHDRBLOPEHSDRBL_CATEGORY =", lvhFHDRBLOPEHSDRBL_CATEGORY);
                log.info("{} {}", "lvhFHDRBLOPEHSCLAIM_TIME =", lvhFHDRBLOPEHSCLAIM_TIME);

                log.info("{}", "Opening cursor cFHDRBLOPEHS4.");

                int t_len1 = 1000;
                int count1 = 0;
                strDurableOperationHistoryFillInODRBQ020DRout = new ArrayList<>(t_len1);

                log.info("{}", "Start fetch data...");
                for (Object[] objects : cFHDRBLOPEHS4) {
                    tmpFHDRBLOPEHSCLAIM_USER_ID = "";

                    String hFHDRBLOPEHSDURABLE_ID = CimObjectUtils.toString(objects[0]);
                    String hFHDRBLOPEHSDRBL_CATEGORY = CimObjectUtils.toString(objects[1]);
                    String hFHDRBLOPEHSMAINPD_ID = CimObjectUtils.toString(objects[2]);
                    String hFHDRBLOPEHSOPE_NO = CimObjectUtils.toString(objects[3]);
                    String hFHDRBLOPEHSPD_ID = CimObjectUtils.toString(objects[4]);
                    String hFHDRBLOPEHSOPE_PASS_COUNT = CimObjectUtils.toString(objects[5]);
                    String hFHDRBLOPEHSPD_NAME = CimObjectUtils.toString(objects[6]);
                    String hFHDRBLOPEHSCLAIM_TIME = CimObjectUtils.toString(objects[7]);
                    tmpFHDRBLOPEHSCLAIM_USER_ID = CimObjectUtils.toString(objects[8]);
                    String hFHDRBLOPEHSMOVE_TYPE = CimObjectUtils.toString(objects[9]);
                    String hFHDRBLOPEHSOPE_CATEGORY = CimObjectUtils.toString(objects[10]);
                    String hFHDRBLOPEHSSTAGE_ID = CimObjectUtils.toString(objects[11]);
                    String hFHDRBLOPEHSSTAGEGRP_ID = CimObjectUtils.toString(objects[12]);
                    String hFHDRBLOPEHSPHOTO_LAYER = CimObjectUtils.toString(objects[13]);
                    String hFHDRBLOPEHSLOCATION_ID = CimObjectUtils.toString(objects[14]);
                    String hFHDRBLOPEHSAREA_ID = CimObjectUtils.toString(objects[15]);
                    String hFHDRBLOPEHSEQP_ID = CimObjectUtils.toString(objects[16]);
                    String hFHDRBLOPEHSEQP_NAME = CimObjectUtils.toString(objects[17]);
                    String hFHDRBLOPEHSOPE_MODE = CimObjectUtils.toString(objects[18]);
                    String hFHDRBLOPEHSLC_RECIPE_ID = CimObjectUtils.toString(objects[19]);
                    String hFHDRBLOPEHSRECIPE_ID = CimObjectUtils.toString(objects[20]);
                    String hFHDRBLOPEHSPH_RECIPE_ID = CimObjectUtils.toString(objects[21]);
                    String hFHDRBLOPEHSRPARM_COUNT = CimObjectUtils.toString(objects[22]);
                    String hFHDRBLOPEHSINIT_HOLD_FLAG = CimObjectUtils.toString(objects[23]);
                    String hFHDRBLOPEHSHOLD_TIME = CimObjectUtils.toString(objects[24]);
                    String hFHDRBLOPEHSHOLD_USER_ID = CimObjectUtils.toString(objects[25]);
                    String hFHDRBLOPEHSHOLD_TYPE = CimObjectUtils.toString(objects[26]);
                    String hFHDRBLOPEHSHOLD_REASON_CODE = CimObjectUtils.toString(objects[27]);
                    String hFHDRBLOPEHSHOLD_REASON_DESC = CimObjectUtils.toString(objects[28]);
                    String hFHDRBLOPEHSREASON_CODE = CimObjectUtils.toString(objects[29]);
                    String hFHDRBLOPEHSREASON_DESCRIPTION = CimObjectUtils.toString(objects[30]);
                    String hFHDRBLOPEHSBANK_ID = CimObjectUtils.toString(objects[31]);
                    String hFHDRBLOPEHSPREV_BANK_ID = CimObjectUtils.toString(objects[32]);
                    String hFHDRBLOPEHSPREV_MAINPD_ID = CimObjectUtils.toString(objects[33]);
                    String hFHDRBLOPEHSPREV_OPE_NO = CimObjectUtils.toString(objects[34]);
                    String hFHDRBLOPEHSPREV_PD_ID = CimObjectUtils.toString(objects[35]);
                    String hFHDRBLOPEHSPREV_PD_NAME = CimObjectUtils.toString(objects[36]);
                    String hFHDRBLOPEHSPREV_PASS_COUNT = CimObjectUtils.toString(objects[37]);
                    String hFHDRBLOPEHSPREV_STAGE_ID = CimObjectUtils.toString(objects[38]);
                    String hFHDRBLOPEHSPREV_STAGEGRP_ID = CimObjectUtils.toString(objects[39]);
                    String hFHDRBLOPEHSPREV_PHOTO_LAYER = CimObjectUtils.toString(objects[40]);
                    String hFHDRBLOPEHSDCTRL_JOB = CimObjectUtils.toString(objects[41]);
                    String hFHDRBLOPEHSREWORK_COUNT = CimObjectUtils.toString(objects[42]);
                    String hFHDRBLOPEHSDRBL_OWNER_ID = CimObjectUtils.toString(objects[43]);
                    String hFHDRBLOPEHSPLAN_END_TIME = CimObjectUtils.toString(objects[44]);
                    String hFHDRBLOPEHSCRITERIA_FLAG = CimObjectUtils.toString(objects[45]);
                    String hFHDRBLOPEHSCLAIM_MEMO = CimObjectUtils.toString(objects[46]);
                    String hFHDRBLOPEHSSTORE_TIME = CimObjectUtils.toString(objects[47]);
                    String hFHDRBLOPEHSRPARM_CHANGE_TYPE = CimObjectUtils.toString(objects[48]);
                    String hFHDRBLOPEHSHOLD_OPE_NO = CimObjectUtils.toString(objects[49]);
                    String hFHDRBLOPEHSHOLD_REASON_OPE_NO = CimObjectUtils.toString(objects[50]);

                    if (count1 >= t_len1) {
                        t_len1 = t_len1 + 500;
                    }

                    if (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDOPERATION)
                            || CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEBACKWARDSTAGE)) {
                        log.info("{}", "Fetch backward movement record");

                        if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD)
                                || CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                        BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) {
                            log.info("{}", "Fetch backward movement record - ignore record");
                            continue;
                        } else {
                            log.info("{}", "Fetch backward movement record - get record");

                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .add(new Infos.DurableOperationHisInfo());
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setRouteID(hFHDRBLOPEHSMAINPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationNumber(hFHDRBLOPEHSOPE_NO);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationID(hFHDRBLOPEHSPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationName(hFHDRBLOPEHSPD_NAME);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageID(hFHDRBLOPEHSSTAGE_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                        }
                    } else if (CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                            BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDOPERATION) ||
                            CimStringUtils.equals(hFHDRBLOPEHSMOVE_TYPE,
                                    BizConstant.SP_MOVEMENTTYPE_MOVEFORWARDSTAGE)) {
                        log.info("{}", "Fetch forward movement record");

                        if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_DURABLEHOLDRELEASE) ||
                                CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                        BizConstant.SP_OPERATIONCATEGORY_OPERATIONCOMPLETE) ||
                                CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                        BizConstant.SP_OPERATIONCATEGORY_GATEPASS)) {
                            log.info("{}", "Fetch forward movement record (1)- get record");

                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .add(new Infos.DurableOperationHisInfo());
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setRouteID(hFHDRBLOPEHSPREV_MAINPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationNumber(hFHDRBLOPEHSPREV_OPE_NO);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationPass(hFHDRBLOPEHSPREV_PASS_COUNT);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationID(hFHDRBLOPEHSPREV_PD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationName(hFHDRBLOPEHSPREV_PD_NAME);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageID(hFHDRBLOPEHSPREV_STAGE_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageGroupID(hFHDRBLOPEHSPREV_STAGEGRP_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setMaskLevel(hFHDRBLOPEHSPREV_PHOTO_LAYER);
                        } else if (CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                BizConstant.SP_OPERATIONCATEGORY_LOCATEBACKWARD) ||
                                CimStringUtils.equals(hFHDRBLOPEHSOPE_CATEGORY,
                                        BizConstant.SP_OPERATIONCATEGORY_LOCATEFORWARD)) {
                            log.info("{}", "Fetch forward movement record (2)- get record");

                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .add(new Infos.DurableOperationHisInfo());
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setRouteID(hFHDRBLOPEHSMAINPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationNumber(hFHDRBLOPEHSOPE_NO);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationID(hFHDRBLOPEHSPD_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setOperationName(hFHDRBLOPEHSPD_NAME);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageID(hFHDRBLOPEHSSTAGE_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                            strDurableOperationHistoryFillInODRBQ020DRout
                                    .get(count1).setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                        } else {
                            log.info("{}", "Fetch forward movement record - ignore record");
                            continue;
                        }
                    } else {
                        log.info("{}", "Fetch non movement record - get record");

                        strDurableOperationHistoryFillInODRBQ020DRout
                                .add(new Infos.DurableOperationHisInfo());
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setRouteID(hFHDRBLOPEHSMAINPD_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setOperationNumber(hFHDRBLOPEHSOPE_NO);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setOperationPass(hFHDRBLOPEHSOPE_PASS_COUNT);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setOperationID(hFHDRBLOPEHSPD_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setOperationName(hFHDRBLOPEHSPD_NAME);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setStageID(hFHDRBLOPEHSSTAGE_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setStageGroupID(hFHDRBLOPEHSSTAGEGRP_ID);
                        strDurableOperationHistoryFillInODRBQ020DRout
                                .get(count1).setMaskLevel(hFHDRBLOPEHSPHOTO_LAYER);
                    }

                    log.info("{}", "set other data members");

                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setReportTimeStamp(hFHDRBLOPEHSCLAIM_TIME);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setOperationCategory(hFHDRBLOPEHSOPE_CATEGORY);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setLocationID(hFHDRBLOPEHSLOCATION_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setWorkArea(hFHDRBLOPEHSAREA_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setEquipmentID(hFHDRBLOPEHSEQP_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setEquipmentName(hFHDRBLOPEHSEQP_NAME);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setOperationMode(hFHDRBLOPEHSOPE_MODE);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setLogicalRecipeID(hFHDRBLOPEHSLC_RECIPE_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setMachineRecipeID(hFHDRBLOPEHSRECIPE_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setPhysicalRecipeID(hFHDRBLOPEHSPH_RECIPE_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setDurableControlJobID(hFHDRBLOPEHSDCTRL_JOB);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setReworkCount(CimLongUtils.longValue(hFHDRBLOPEHSREWORK_COUNT));
                    String buffer1;
                    buffer1 = hFHDRBLOPEHSINIT_HOLD_FLAG;
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setInitialHoldFlag(buffer1);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setHoldType(hFHDRBLOPEHSHOLD_TYPE);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setHoldTimeStamp(hFHDRBLOPEHSHOLD_TIME);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setHoldUserID(hFHDRBLOPEHSHOLD_USER_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setHoldReasonCodeID(hFHDRBLOPEHSHOLD_REASON_CODE);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setHoldReasonCodeDescription(hFHDRBLOPEHSHOLD_REASON_DESC);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setReasonCodeID(hFHDRBLOPEHSREASON_CODE);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setReasonCodeDescription(hFHDRBLOPEHSREASON_DESCRIPTION);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setBankID(hFHDRBLOPEHSBANK_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setPreviousBankID(hFHDRBLOPEHSPREV_BANK_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setDurableOwnerID(hFHDRBLOPEHSDRBL_OWNER_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setDueTimeStamp(hFHDRBLOPEHSPLAN_END_TIME);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setTestCriteriaFlag(CimBooleanUtils.isTrue(hFHDRBLOPEHSCRITERIA_FLAG));
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setUserID(tmpFHDRBLOPEHSCLAIM_USER_ID);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setClaimMemo(hFHDRBLOPEHSCLAIM_MEMO);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setStoreTimeStamp(hFHDRBLOPEHSSTORE_TIME);
                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setRecipeParameterChangeType(hFHDRBLOPEHSRPARM_CHANGE_TYPE);

                    // (*3)  set pptOperationHisInfo[i].strOpeHisRecipeParmInfo
                    List<Infos.OpeHisRecipeParmInfo> strDurableOpeHisRParmGetDROut;
                    Infos.DurableOpeHisRParmGetDRIn strDurableOpeHisRParmGetDRin
                            = new Infos.DurableOpeHisRParmGetDRIn();
                    strDurableOpeHisRParmGetDRin
                            .setDurableCategory(hFHDRBLOPEHSDRBL_CATEGORY);
                    strDurableOpeHisRParmGetDRin
                            .setDurableID(ObjectIdentifier.buildWithValue(hFHDRBLOPEHSDURABLE_ID));
                    strDurableOpeHisRParmGetDRin
                            .setRouteID(hFHDRBLOPEHSMAINPD_ID);
                    strDurableOpeHisRParmGetDRin
                            .setOperationNumber(hFHDRBLOPEHSOPE_NO);
                    strDurableOpeHisRParmGetDRin
                            .setOperationPassCount(CimLongUtils.longValue(hFHDRBLOPEHSOPE_PASS_COUNT));
                    strDurableOpeHisRParmGetDRin
                            .setClaimTime(hFHDRBLOPEHSCLAIM_TIME);
                    strDurableOpeHisRParmGetDRin
                            .setOperationCategory(hFHDRBLOPEHSOPE_CATEGORY);
                    strDurableOpeHisRParmGetDROut = durableOpeHisRParmGetDR(strObjCommonIn,
                            strDurableOpeHisRParmGetDRin);

                    strDurableOperationHistoryFillInODRBQ020DRout
                            .get(count1).setStrOpeHisRecipeParmInfo(strDurableOpeHisRParmGetDROut);
                    count1++;

                    if (count1 >= lenOpeHisMax) {
                        log.info("{} {}", " Length of OpeHis reached the maximum ", count1);
                        break;
                    }
                }
            }
        }

        return strDurableOperationHistoryFillInODRBQ020DRout;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableOpeHisRParmGetDRin
     * @return java.util.List<com.fa.cim.dto.Infos.OpeHisRecipeParmInfo>
     * @throws
     * @author ho
     * @date 2020/6/22 15:07
     */
    public List<Infos.OpeHisRecipeParmInfo> durableOpeHisRParmGetDR(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOpeHisRParmGetDRIn strDurableOpeHisRParmGetDRin) {
        log.info("PPTManager_i::durable_opeHisRParm_GetDR");

        log.info("{} {}", "in param [durableID]         ", strDurableOpeHisRParmGetDRin.getDurableID().getValue());
        log.info("{} {}", "in param [durableCategory]   ", strDurableOpeHisRParmGetDRin.getDurableCategory());
        log.info("{} {}", "in param [routeID]           ", strDurableOpeHisRParmGetDRin.getRouteID());
        log.info("{} {}", "in param [operationNumber]   ", strDurableOpeHisRParmGetDRin.getOperationNumber());
        log.info("{} {}", "in param [operationPassCount]", strDurableOpeHisRParmGetDRin.getOperationPassCount());
        log.info("{} {}", "in param [claimTime]         ", strDurableOpeHisRParmGetDRin.getClaimTime());
        log.info("{} {}", "in param [operationCategory] ", strDurableOpeHisRParmGetDRin.getOperationCategory());


        /*----------------------------------------*/
        /*   Get RecipeParameter Sequence Count   */
        /*----------------------------------------*/
        log.info("{}", "Get RecipeParameter Sequence Count");
        int nRCPParmSeqLen = 10;
        String theRCPParmSeqLen = StandardProperties.OM_OPEHIST_RCPPARM_EXTLEN_FOR_INQ.getValue();
        if (0 < CimStringUtils.length(theRCPParmSeqLen)) {
            nRCPParmSeqLen = CimNumberUtils.intValue(theRCPParmSeqLen);
            log.info("{} {}", "nRCPParmSeqLen", nRCPParmSeqLen);
        }

        /*----------------------------*/
        /*   Get OHLOTOPE_RPARAM data   */
        /*----------------------------*/
        log.info("{}", "Get OHLOTOPE_RPARAM data");
        String hFHDRBLOPEHS_RPARMDURABLE_ID = strDurableOpeHisRParmGetDRin.getDurableID().getValue();
        String hFHDRBLOPEHS_RPARMDRBL_CATEGORY = strDurableOpeHisRParmGetDRin.getDurableCategory();
        String hFHDRBLOPEHS_RPARMMAINPD_ID = strDurableOpeHisRParmGetDRin.getRouteID();
        String hFHDRBLOPEHS_RPARMOPE_NO = strDurableOpeHisRParmGetDRin.getOperationNumber();
        String hFHDRBLOPEHS_RPARMCLAIM_TIME = strDurableOpeHisRParmGetDRin.getClaimTime();
        String hFHDRBLOPEHS_RPARMOPE_CATEGORY = strDurableOpeHisRParmGetDRin.getOperationCategory();
        Long hFHDRBLOPEHS_RPARMOPE_PASS_COUNT = strDurableOpeHisRParmGetDRin.getOperationPassCount();

        List<Object[]> cFHDRBLOPEHS_RPARM = cimJpaRepository.query("SELECT RPARAM_NAME, RPARAM_VAL\n" +
                        "        FROM OHDUROPE_RPARAM\n" +
                        "        WHERE DRBL_ID     = ?\n" +
                        "        AND DRBL_CATEGORY  = ?\n" +
                        "        AND PROCESS_ID      = ?\n" +
                        "        AND OPE_NO         = ?\n" +
                        "        AND OPE_PASS_COUNT = ?\n" +
                        "        AND TRX_TIME     = ?\n" +
                        "        AND OPE_CATEGORY   = ?", hFHDRBLOPEHS_RPARMDURABLE_ID
                , hFHDRBLOPEHS_RPARMDRBL_CATEGORY
                , hFHDRBLOPEHS_RPARMMAINPD_ID
                , hFHDRBLOPEHS_RPARMOPE_NO
                , hFHDRBLOPEHS_RPARMOPE_PASS_COUNT
                , hFHDRBLOPEHS_RPARMCLAIM_TIME
                , hFHDRBLOPEHS_RPARMOPE_CATEGORY);

        int lenSeq = nRCPParmSeqLen;
        int lenOpeHisRParm = 0;
        List<Infos.OpeHisRecipeParmInfo> strOpeHisRecipeParmInfo;
        strOpeHisRecipeParmInfo = new ArrayList<>(lenSeq);

        for (Object[] objects : cFHDRBLOPEHS_RPARM) {
            String hFHDRBLOPEHS_RPARMRPARM_NAME = "";
            String hFHDRBLOPEHS_RPARMRPARM_VALUE = "";

            hFHDRBLOPEHS_RPARMRPARM_NAME = CimObjectUtils.toString(objects[0]);
            hFHDRBLOPEHS_RPARMRPARM_VALUE = CimObjectUtils.toString(objects[1]);

            log.info("{} {}", "  RPARM_NAME......", hFHDRBLOPEHS_RPARMRPARM_NAME);
            log.info("{} {}", "  RPARM_VALUE.....", hFHDRBLOPEHS_RPARMRPARM_VALUE);

            if (lenOpeHisRParm >= lenSeq) {
                lenSeq = lenSeq + nRCPParmSeqLen;
            }

            strOpeHisRecipeParmInfo.add(new Infos.OpeHisRecipeParmInfo());
            strOpeHisRecipeParmInfo.get(lenOpeHisRParm).setRecipeParameterName(hFHDRBLOPEHS_RPARMRPARM_NAME);
            strOpeHisRecipeParmInfo.get(lenOpeHisRParm).setRecipeParameterValue(hFHDRBLOPEHS_RPARMRPARM_VALUE);
            lenOpeHisRParm++;
        }

        log.info("{} {}", "lenOpeHisRParm", lenOpeHisRParm);

        // Debug Trace Output Parameter
        log.info("{}", "------- Debug Output Parameter [strOpeHisRecipeParmInfo] -------");
        for (int nOpeHisRcpParmSeq = 0; nOpeHisRcpParmSeq < CimArrayUtils.getSize(strOpeHisRecipeParmInfo); nOpeHisRcpParmSeq++) {
            log.info("{} {}", "recipeParameterName____________", strOpeHisRecipeParmInfo.get(nOpeHisRcpParmSeq).getRecipeParameterName());
            log.info("{} {}", "recipeParameterValue___________", strOpeHisRecipeParmInfo.get(nOpeHisRcpParmSeq).getRecipeParameterValue());
        }

        /*--------------------------*/
        /*   Set Output Parameter   */
        /*--------------------------*/
        return strOpeHisRecipeParmInfo;
    }

    @Override
    public void durableCheckForDeletion(Infos.ObjCommon objCommon, String className, ObjectIdentifier durableID) {
        //-------------------------------------------------------
        // Check whether the target object can be deleted or not
        //-------------------------------------------------------
        List<GlobalDTO.CheckMessage> aMessageList = new ArrayList<>();
        if (CimStringUtils.equals(className, BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            aMessageList = aCassette.checkForDeletion(CimNumberUtils.intValue(BizConstant.SP_CHECK_MSG_DEFAULT_MAX_COUNT));
        } else if (CimStringUtils.equals(className, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            aMessageList = aReticlePod.checkForDeletion(CimNumberUtils.intValue(BizConstant.SP_CHECK_MSG_DEFAULT_MAX_COUNT));
        }

        int msgLen = aMessageList.size();
        if (msgLen > 0) {
            log.info("The target object can not be deleted.");
            throw new ServiceException(retCodeConfigEx.getNotAllowedDelete());
        }
    }

    @Override
    public Infos.DurableAttribute durableDelete(Infos.ObjCommon objCommon, String className, ObjectIdentifier durableID) {
        //-------------------------------------------------------
        // Delete the target object
        //-------------------------------------------------------
        Infos.DurableAttribute strDurable_Delete_out = new Infos.DurableAttribute();
        if (CimStringUtils.equals(className, BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            durableManager.removeCassette(aCassette);
            //CALL cassette_DBInfo_GetDR__170
            Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfo = new Infos.CassetteDBINfoGetDRInfo();
            cassetteDBINfoGetDRInfo.setCassetteID(durableID);
            cassetteDBINfoGetDRInfo.setDurableOperationInfoFlag(false);
            cassetteDBINfoGetDRInfo.setDurableWipOperationInfoFlag(false);
            Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDROut = cassetteMethod.cassetteDBInfoGetDR(objCommon, cassetteDBINfoGetDRInfo);
            Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = cassetteDBInfoGetDROut.getCarrierDetailInfoInqResult();
            strDurable_Delete_out.setDurableID(carrierDetailInfoInqResult.getCassetteID());
            strDurable_Delete_out.setDescription(carrierDetailInfoInqResult.getCassetteBRInfo().getDescription());
            strDurable_Delete_out.setCategory(carrierDetailInfoInqResult.getCassetteBRInfo().getCassetteCategory());
            strDurable_Delete_out.setUsageCheckFlag(carrierDetailInfoInqResult.getCassetteBRInfo().isUsageCheckFlag());
            strDurable_Delete_out.setMaximumRunTime(carrierDetailInfoInqResult.getCassettePMInfo().getMaximumRunTime());
            strDurable_Delete_out.setMaximumOperationStartCount(carrierDetailInfoInqResult.getCassettePMInfo().getMaximumOperationStartCount().doubleValue());
            strDurable_Delete_out.setIntervalBetweenPM(carrierDetailInfoInqResult.getCassettePMInfo().getIntervalBetweenPM().intValue());
            strDurable_Delete_out.setCapacity(carrierDetailInfoInqResult.getCassetteBRInfo().getCapacity().intValue());
            strDurable_Delete_out.setNominalSize(carrierDetailInfoInqResult.getCassetteBRInfo().getNominalSize().intValue());
            strDurable_Delete_out.setContents(carrierDetailInfoInqResult.getCassetteBRInfo().getContents());
            strDurable_Delete_out.setInstanceName(carrierDetailInfoInqResult.getCassetteLocationInfo().getInstanceName());
        } else if (CimStringUtils.equals(className, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            durableManager.removeReticlePodNamed(aReticlePod);
            Params.ReticlePodDetailInfoInqParams params = new Params.ReticlePodDetailInfoInqParams();
            params.setDurableOperationInfoFlag(false);
            params.setDurableWipOperationInfoFlag(false);
            params.setReticlePodID(durableID);
            Outputs.ObjReticlePodFillInTxPDQ013DROut objReticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, params);
            strDurable_Delete_out.setDurableID(objReticlePodFillInTxPDQ013DROut.getReticlePodID());
            strDurable_Delete_out.setDescription(objReticlePodFillInTxPDQ013DROut.getReticlePodBRInfo().getDescription());
            strDurable_Delete_out.setCategory(objReticlePodFillInTxPDQ013DROut.getReticlePodBRInfo().getReticlePodCategory());
            strDurable_Delete_out.setUsageCheckFlag(false);
            strDurable_Delete_out.setMaximumOperationStartCount(0d);
            strDurable_Delete_out.setIntervalBetweenPM((int) objReticlePodFillInTxPDQ013DROut.getReticlePodPMInfo().getIntervalBetweenPM());
            strDurable_Delete_out.setCapacity((int) objReticlePodFillInTxPDQ013DROut.getReticlePodBRInfo().getCapacity());
            strDurable_Delete_out.setNominalSize(0);
            strDurable_Delete_out.setInstanceName(objReticlePodFillInTxPDQ013DROut.getReticlePodLocationInfo().getInstanceName());
        }
        return strDurable_Delete_out;
    }

    @Override
    public void durableProcessStateMakeProcessing(Infos.ObjCommon objCommon, String durableCategory, List<Infos.StartDurable> startDurables) {
        Validations.check(null == objCommon || null == durableCategory || null == startDurables, retCodeConfig.getInvalidInputParam());

        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());
        Optional.of(startDurables).ifPresent(list -> list.forEach(startDurable -> {
            switch (durableCategory) {
                case SP_DURABLECAT_CASSETTE:
                    CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startDurable.getDurableId());
                    Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
                    aCassette.makeProcessing();
                    aCassette.setLastClaimedPerson(aPerson);
                    aCassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                    aCassette.setStateChangedPerson(aPerson);
                    aCassette.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                    break;
                case SP_DURABLECAT_RETICLEPOD:
                    CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, startDurable.getDurableId());
                    Validations.check(null == aReticlePod, retCodeConfig.getNotFoundReticlePod());
                    aReticlePod.makeProcessing();
                    aReticlePod.setLastClaimedPerson(aPerson);
                    aReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                    aReticlePod.setStateChangedPerson(aPerson);
                    aReticlePod.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                    break;
                case SP_DURABLECAT_RETICLE:
                    CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, startDurable.getDurableId());
                    Validations.check(null == aReticle, retCodeConfig.getNotFoundReticle());
                    aReticle.makeProcessing();
                    aReticle.setLastClaimedPerson(aPerson);
                    aReticle.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                    aReticle.setStateChangedPerson(aPerson);
                    aReticle.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                    break;
                default:
                    Validations.check(retCodeConfig.getInvalidDurableCategory());
                    break;
            }
        }));
    }

    @Override
    public void durablePFXCreate(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, ObjectIdentifier routeID) {
        Validations.check(null == objCommon || null == durableCategory, retCodeConfig.getInvalidInputParam());
        log.info("in-parm durableCategory  : " + durableCategory);
        log.info("in-parm durableID        : " + ObjectIdentifier.fetchValue(routeID));

        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());

        boolean bOnRouteReqFlag = false;
        boolean bIsOnRoute = false;
        CimProcessDefinition mainPD = null;
        CimDurableProcessFlowContext aDurablePFX;
        CimBank aBank;
        if (ObjectIdentifier.isNotEmptyWithValue(routeID)) {
            log.info("routeID is not blank");
            bOnRouteReqFlag = true;
            mainPD = baseCoreFactory.getBO(CimProcessDefinition.class, routeID);
            Validations.check(null == mainPD, retCodeConfig.getNotFoundRoute());
        }

        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundCassette());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticle());
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }

        //---------------------------------------
        // Get Durable PFX
        //---------------------------------------
        assert aDurable != null;
        aDurablePFX = aDurable.getDurableProcessFlowContext();

        if (null != aDurablePFX) {
            log.info("DurablePFX exists");
            //-----------------------------------------------------
            // Delete the past processed operation information
            //-----------------------------------------------------
            aDurable.deleteDurableProcessFlowContext();

            //---------------------------------------
            // OnRoute check
            //---------------------------------------
            bIsOnRoute = aDurable.isOnRoute();

            if (bIsOnRoute) {
                // Reset OnRoute information
                aDurable.resetOnRouteState();
            }
        }

        if (bOnRouteReqFlag) {
            log.info("bOnRouteReqFlag == TRUE");
            aDurable.setMainProcessDefinition(mainPD);

            // Change the Flow status for Durable.
            try {
                aDurable.makeInProduction();
                aDurable.makeNotOnHold();
                aDurable.makeWaiting();
                aDurable.makeOnFloor();
            } catch (Exception e) {
                throw new ServiceException(retCodeConfig.getInvalidStateTransition());
            }
            aBank = aDurable.getBank();
            aDurable.setPreviousBank(aBank);
            aDurable.setBank(null);

            Boolean isBankInRequired = aDurable.isBankInRequired();
            if (isBankInRequired) {
                log.info("isBankInRequired is TRUE");
                aDurable.makeBankInRequired();
            } else {
                aDurable.makeNotBankInRequired();
            }
        } else {
            log.info("bOnRouteReqFlag != TRUE");
            aBank = aDurable.getBank();
            if (null != aBank) {
                aDurable.setAllStateForBankInCancel();
                aDurable.setPreviousBank(aBank);
                aDurable.setBank(null);
                aDurable.makeNotBankInRequired();
            }
        }

        // Begin Next Durable PO
        aDurable.beginNextDurableProcessOperation();

        aDurable.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aDurable.setLastClaimedPerson(aPerson);

        if (bOnRouteReqFlag) {
            aDurable.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aDurable.setStateChangedPerson(aPerson);
            aDurable.setInventoryStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aDurable.setInventoryStateChangedPerson(aPerson);
        }

        if (bOnRouteReqFlag) {
            // For What's Next
            dispatchingManager.addToDurableQueue(durableCategory, durableID);
        }
    }

    @Override
    public void durablePFXDelete(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        Validations.check(null == objCommon || null == durableCategory, retCodeConfig.getInvalidInputParam());
        log.info("in-parm durableCategory  : " + durableCategory);

        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());

        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundCassette());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticle());
                break;
        }

        assert aDurable != null;
        aDurable.deleteDurableProcessFlowContext();
        if (aDurable.isOnRoute()) {
            // Get Durables' Main PD
            CimProcessDefinition mainPD = aDurable.getMainProcessDefinition();
            Validations.check(null == mainPD, retCodeConfig.getNotFoundRoute());

            // Get Start Bank
            CimBank startBank = mainPD.getStartBank();
            Validations.check(null == startBank, retCodeConfig.getNotFoundBank());

            // Reset OnRoute/State information
            aDurable.resetOnRouteState();

            aDurable.setBank(startBank);

            if (!aDurable.isInBank()) {
                aDurable.makeInBank();
                aDurable.makeNotBankInRequired();
            }
        }
        aDurable.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aDurable.setLastClaimedPerson(aPerson);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableControlJobstartReserveInformationGetin
     * @return com.fa.cim.dto.Infos.DurableControlJobStartReserveInformationGetOut
     * @throws
     * @author ho
     * @date 2020/6/23 16:53
     */
    public Infos.DurableControlJobStartReserveInformationGetOut durableControlJobStartReserveInformationGet(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableControlJobStartReserveInformationGetIn strDurableControlJobstartReserveInformationGetin) {
        Infos.DurableControlJobStartReserveInformationGetOut strDurableControlJobstartReserveInformationGetout = new Infos.DurableControlJobStartReserveInformationGetOut();
        //-----------------------------------//
        //  Get PosDurableControlJob object  //
        //-----------------------------------//
        CimDurableControlJob aDurableControlJob;
        aDurableControlJob = baseCoreFactory.getBO(CimDurableControlJob.class,
                strDurableControlJobstartReserveInformationGetin.getDurableControlJobID());

        /*-------------------------------------------*/
        /*   Get PosStartDurableSequence Info        */
        /*-------------------------------------------*/
        List<DurableDTO.StartDurableInfo> startDurableSequence = null;
        List<DurableDTO.StartDurableInfo> startDurableSequenceVar;

        startDurableSequence = aDurableControlJob.getStartDurableInfo();
        startDurableSequenceVar = startDurableSequence;

        /*-----------------------------*/
        /*   Get DurableCategory Info  */
        /*-----------------------------*/
        String durableCategory;
        durableCategory = aDurableControlJob.getDurableCategory();

        strDurableControlJobstartReserveInformationGetout.setDurableCategory(durableCategory);

        /*-----------------------------*/
        /*   Get Equipment Info        */
        /*-----------------------------*/
        CimMachine aMachine;
        aMachine = aDurableControlJob.getMachine();

        boolean bCJMachineNil = false;
        if (aMachine == null) {
            log.info("{}", "aMachine is Nil");
            bCJMachineNil = true;
        }

        List<MachineDTO.MachineCassette> strMachineCassetteSeq = null;
        List<MachineDTO.MachineCassette> castSeqVar;

        if (!bCJMachineNil) {
            log.info("{}", "aMachine is not Nil");

            strMachineCassetteSeq = aMachine.allCassettes();
            castSeqVar = strMachineCassetteSeq;

            /*-----------------------------------------------*/
            /*      Set equipmentID to Return Structure      */
            /*-----------------------------------------------*/
            strDurableControlJobstartReserveInformationGetout.setEquipmentID(ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
        }

        int durableLen;
        durableLen = CimArrayUtils.getSize(startDurableSequence);

        //--------------------------------------------------//
        //  Set following informations to Return Structure  //
        //--------------------------------------------------//
        strDurableControlJobstartReserveInformationGetout.setStrStartDurables(new ArrayList<>(durableLen));

        for (int i = 0; i < durableLen; i++) {
            log.info("{} {}", "loop to startDurableSequence->length()", durableLen, i);
            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().add(new Infos.StartDurable());
            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(i).setDurableId(startDurableSequence.get(i).getDurableID());
            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(i).setStartDurablePort(new Infos.StartDurablePort());
            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(i).getStartDurablePort().setLoadPurposeType(startDurableSequence.get(i).getLoadPurposeType());
            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(i).getStartDurablePort().setLoadPortID(startDurableSequence.get(i).getLoadPortID());
            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(i).getStartDurablePort().setLoadSequenceNumber(startDurableSequence.get(i).getLoadSequenceNumber());
            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(i).getStartDurablePort().setUnloadPortID(startDurableSequence.get(i).getUnloadPortID());

            //-----------------------------------------------------------------------------------------------------
            //  Get unloadPortID.
            //  Find the unloadPortID of strMachineCassetteSeq that CassetteID is the same as CassetteID of strMachineCassetteSeq
            //-----------------------------------------------------------------------------------------------------
            if (!bCJMachineNil) {
                int lenMacCas = CimArrayUtils.getSize(strMachineCassetteSeq);
                log.info("{} {}", "strMachineCassetteSeq->length", lenMacCas);
                for (int k = 0; k < lenMacCas; k++) {
                    if (CimStringUtils.equals(strMachineCassetteSeq.get(k).getCassetteID().getValue(), startDurableSequence.get(i).getDurableID().getValue())) {
                        strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(i).getStartDurablePort().setUnloadPortID(strMachineCassetteSeq.get(k).getUnloadPortID());
                        break;
                    }
                }
            }
        }

        ProcessDTO.ActualStartInformationForPO actualStartInfo = null;
        for (int durableSeq = 0; durableSeq < durableLen; durableSeq++) {
            boolean strDurableCheckConditionForDurablePOout;
            Infos.DurableCheckConditionForDurablePOIn strDurableCheckConditionForDurablePOin = new Infos.DurableCheckConditionForDurablePOIn();
            strDurableCheckConditionForDurablePOin.setDurableCategory(durableCategory);
            strDurableCheckConditionForDurablePOin.setDurableID(strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId());
            strDurableCheckConditionForDurablePOout = durableCheckConditionForDurablePO(strObjCommonIn, strDurableCheckConditionForDurablePOin);

            CimDurableProcessOperation aPosDurablePO = null;
            boolean isDrblOnRoute = false;
            if (strDurableCheckConditionForDurablePOout) {
                log.info("{}", "strDurableCheckConditionForDurablePOout.currentPOFlag == TRUE");
                if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
                    log.info("{}", "durableCategory is Cassette");
                    CimCassette aCassette;
                    aCassette = baseCoreFactory.getBO(CimCassette.class,
                            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId());
                    aPosDurablePO = aCassette.getDurableProcessOperation();
                } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
                    log.info("{}", "durableCategory is ReticlePod");
                    CimReticlePod aReticlePod;
                    aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId());
                    aPosDurablePO = aReticlePod.getDurableProcessOperation();
                } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
                    log.info("{}", "durableCategory is Reticle");
                    CimProcessDurable aDurable;
                    aDurable = baseCoreFactory.getBO(CimProcessDurable.class,
                            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId());
                    aPosDurablePO = aDurable.getDurableProcessOperation();
                } else {
                    // Continue to the following procedure
                }
            } else {
                log.info("", "strDurableCheckConditionForDurablePOout.currentPOFlag == FALSE");
                if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
                    log.info("{}", "durableCategory is Cassette");
                    CimCassette aCassette;
                    aCassette = baseCoreFactory.getBO(CimCassette.class,
                            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId());
                    aPosDurablePO = aCassette.getPreviousDurableProcessOperation();
                } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
                    log.info("{}", "durableCategory is ReticlePod");
                    CimReticlePod aReticlePod;
                    aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId());
                    aPosDurablePO = aReticlePod.getPreviousDurableProcessOperation();
                } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
                    log.info("{}", "durableCategory is Reticle");
                    CimProcessDurable aDurable;
                    aDurable = baseCoreFactory.getBO(CimProcessDurable.class,
                            strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId());
                    aPosDurablePO = aDurable.getPreviousDurableProcessOperation();
                } else {
                    // Continue to the following procedure
                }
            }

            Validations.check(null == aPosDurablePO, retCodeConfig.getNotFoundDurablePo(),
                    strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId().getValue());

            if (durableSeq == 0) {
                actualStartInfo = aPosDurablePO.getActualStartInfo(true);
            }

            boolean isOnRoute = false;
            try {
                durableOnRouteCheck(strObjCommonIn, durableCategory,
                        strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getDurableId());
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getDurableOnroute())) {
                    isOnRoute = true;
                }
            }

            if (isOnRoute) {
                log.info("{}", "durable is on route");

                //--------------------------//
                //   RouteID                //
                //--------------------------//
                CimProcessDefinition aMainPD = aPosDurablePO.getMainProcessDefinition();
                Validations.check(null == aMainPD, retCodeConfig.getNotFoundRoute());
                strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).setStartOperationInfo(new Infos.StartOperationInfo());
                strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getStartOperationInfo().setProcessFlowID(
                        ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));

                //--------------------------//
                //   PDID                   //
                //--------------------------//
                CimProcessDefinition aPD = aPosDurablePO.getProcessDefinition();
                Validations.check(null == aPD, retCodeConfig.getNotFoundProcessDefinition());
                strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getStartOperationInfo().setOperationID(
                        ObjectIdentifier.build(aPD.getIdentifier(), aPD.getPrimaryKey()));

                //--------------------------//
                //   OperationNumber        //
                //--------------------------//
                strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getStartOperationInfo().setOperationNumber(aPosDurablePO.getOperationNumber());

                //--------------------------//
                //   PassCount              //
                //--------------------------//
                strDurableControlJobstartReserveInformationGetout.getStrStartDurables().get(durableSeq).getStartOperationInfo().setPassCount(CimNumberUtils.intValue(aPosDurablePO.getPassCount()));
            } else {
                log.info("{}", "durable is not on route");
            }
        }

        strDurableControlJobstartReserveInformationGetout.setStrDurableStartRecipe(new Infos.DurableStartRecipe());
        strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().setLogicalRecipeId(actualStartInfo.getAssignedLogicalRecipe());
        strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().setMachineRecipeId(actualStartInfo.getAssignedMachineRecipe());
        strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().setPhysicalRecipeId(actualStartInfo.getAssignedPhysicalRecipe());

        int rpsetLen;
        rpsetLen = CimArrayUtils.getSize(actualStartInfo.getAssignedRecipeParameterSets());

        int rparmLen;

        if (rpsetLen == 1) {
            log.info("{}", "rpsetLen == 1");
            rparmLen = CimArrayUtils.getSize(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList());
            log.info("{} {}", "actualStartInfo->assignedRecipeParameterSets[0].recipeParameters.length--->", rparmLen);

            if (rparmLen == 1) {
                log.info("{}", "rparmLen == 1");
                if (CimStringUtils.length(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList().get(0).getParameterName()) == 0 &&
                        CimStringUtils.length(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList().get(0).getParameterValue()) == 0 &&
                        CimStringUtils.length(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList().get(0).getTargetValue()) == 0) {
                    log.info("{}", "set rpsetLen = 0");
                    rpsetLen = 0;
                }
            }
        }

        if (rpsetLen > 0) {
            log.info("{}", "actualStartInfo->assignedRecipeParameterSets.length() > 0");
            rparmLen = CimArrayUtils.getSize(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList());
            strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().setStartRecipeParameterS(new ArrayList<>(rparmLen));

            for (int rp = 0; rp < rparmLen; rp++) {
                log.info("{} {}", "loop to actualStartInfo->assignedRecipeParameterSets[0].recipeParameters.length()", rparmLen, rp);
                strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().getStartRecipeParameterS().add(new Infos.StartRecipeParameter());
                strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().getStartRecipeParameterS().get(rp).setParameterName(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList().get(rp).getParameterName());
                strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().getStartRecipeParameterS().get(rp).setParameterValue(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList().get(rp).getParameterValue());
                strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().getStartRecipeParameterS().get(rp).setTargetValue(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList().get(rp).getTargetValue());
                strDurableControlJobstartReserveInformationGetout.getStrDurableStartRecipe().getStartRecipeParameterS().get(rp).setUseCurrentSettingValueFlag(actualStartInfo.getAssignedRecipeParameterSets().get(0).getRecipeParameterList().get(rp).getUseCurrentSettingValueFlag());
            }
        }
        //--------------------//
        //  Return to Caller  //
        //--------------------//
        log.info("PPTManager_i::durableControlJob_startReserveInformation_Get");
        return strDurableControlJobstartReserveInformationGetout;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableprocessStateMakeWaitingin
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/24 14:36
     */
    public void durableProcessStateMakeWaiting(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableProcessStateMakeWaitingIn strDurableprocessStateMakeWaitingin) {
        log.info("PPTManager_i::durable_processState_MakeWaiting");

        CimPerson aPerson;
        aPerson = baseCoreFactory.getBO(CimPerson.class, strObjCommonIn.getUser().getUserID());

        int durableLen = CimArrayUtils.getSize(strDurableprocessStateMakeWaitingin.getStrStartDurables());
        for (int durableSeq = 0; durableSeq < durableLen; durableSeq++) {
            if (CimStringUtils.equals(strDurableprocessStateMakeWaitingin.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
                log.info("{}", "durableCategory is Cassette");
                CimCassette aCassette;
                aCassette = baseCoreFactory.getBO(CimCassette.class,
                        strDurableprocessStateMakeWaitingin.getStrStartDurables().get(durableSeq).getDurableId());
                aCassette.makeWaiting();

                aCassette.setLastClaimedPerson(aPerson);

                aCassette.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

                aCassette.setStateChangedPerson(aPerson);

                aCassette.setStateChangedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());
            } else if (CimStringUtils.equals(strDurableprocessStateMakeWaitingin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
                log.info("{}", "durableCategory is ReticlePod");
                CimReticlePod aReticlePod;
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                        strDurableprocessStateMakeWaitingin.getStrStartDurables().get(durableSeq).getDurableId());

                aReticlePod.makeWaiting();

                aReticlePod.setLastClaimedPerson(aPerson);

                aReticlePod.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

                aReticlePod.setStateChangedPerson(aPerson);

                aReticlePod.setStateChangedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());
            } else if (CimStringUtils.equals(strDurableprocessStateMakeWaitingin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
                log.info("{}", "durableCategory is Reticle");
                CimProcessDurable aReticle;
                aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                        strDurableprocessStateMakeWaitingin.getStrStartDurables().get(durableSeq).getDurableId());

                aReticle.makeWaiting();

                aReticle.setLastClaimedPerson(aPerson);

                aReticle.setLastClaimedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());

                aReticle.setStateChangedPerson(aPerson);

                aReticle.setStateChangedTimeStamp(strObjCommonIn.getTimeStamp().getReportTimeStamp());
            }
        }

        log.info("PPTManager_i::durable_processState_MakeWaiting");
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableCheckConditionForDurablePOin
     * @return boolean
     * @throws
     * @author ho
     * @date 2020/6/24 16:19
     */
    public boolean durableCheckConditionForDurablePO(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableCheckConditionForDurablePOIn strDurableCheckConditionForDurablePOin) {
        log.info("PPTManager_i::durable_CheckConditionForDurablePO");

        //----------------//
        //   Initialize   //
        //----------------//
        boolean strDurableCheckConditionForDurablePOout = true;
        CimDurableControlJob aDurableControlJob = null;

        //--------------------//
        // Get Durable Object //
        //--------------------//
        if (CimStringUtils.equals(strDurableCheckConditionForDurablePOin.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strDurableCheckConditionForDurablePOin.getDurableID());

            //----------------------------------//
            //   Get Durables' Control Job ID   //
            //----------------------------------//
            aDurableControlJob = aCassette.getDurableControlJob();
        } else if (CimStringUtils.equals(strDurableCheckConditionForDurablePOin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strDurableCheckConditionForDurablePOin.getDurableID());

            //----------------------------------//
            //   Get Durables' Control Job ID   //
            //----------------------------------//
            aDurableControlJob = aReticlePod.getDurableControlJob();
        } else if (CimStringUtils.equals(strDurableCheckConditionForDurablePOin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strDurableCheckConditionForDurablePOin.getDurableID());

            //----------------------------------//
            //   Get Durables' Control Job ID   //
            //----------------------------------//
            aDurableControlJob = aReticle.getDurableControlJob();
        }

        if (aDurableControlJob == null) {
            log.info("{}", "aDurableControlJob is nil");
            if (CimStringUtils.equals(strObjCommonIn.getTransactionID(), "ODRBW026")) {
                log.info("{} {}", "set currentPOFlag = FALSE : TxID == ", strObjCommonIn.getTransactionID());
                strDurableCheckConditionForDurablePOout = false;
            }
        } else {
            log.info("{}", "aDurableControlJob is not nil");

            //---------------------------------
            // CJ has a relation with Machine?
            //---------------------------------
            CimMachine aMachine;
            aMachine = aDurableControlJob.getMachine();

            if (aMachine == null) {
                log.info("{}", "aDurableControlJob->aMachine is nil. Control Job doesn't have a relation with Machine.");
                strDurableCheckConditionForDurablePOout = false;
            }
        }
        log.info("{} {}", "currentPOFlag", (strDurableCheckConditionForDurablePOout ? "TRUE" : "FALSE"));

        //----------------------
        //   Return to Caller
        //----------------------
        log.info("PPTManager_i::durable_CheckConditionForDurablePO");

        return (strDurableCheckConditionForDurablePOout);
    }

    @Override
    public boolean durableCheckConditionForDurablePO(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableId) {
        Validations.check(null == objCommon || null == durableCategory || null == durableId, retCodeConfig.getInvalidInputParam());
        boolean retVal = true;
        CimDurableControlJob aDurableControlJob = null;
        //--------------------//
        // Get Durable Object //
        //--------------------//
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableId);
                Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
                aDurableControlJob = aCassette.getDurableControlJob();
                break;
            case SP_DURABLECAT_RETICLEPOD:
                CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableId);
                Validations.check(null == aReticlePod, retCodeConfig.getNotFoundReticlePod());
                aDurableControlJob = aReticlePod.getDurableControlJob();
                break;
            case SP_DURABLECAT_RETICLE:
                CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableId);
                Validations.check(null == aReticle, retCodeConfig.getNotFoundReticle());
                aDurableControlJob = aReticle.getDurableControlJob();
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }
        if (null == aDurableControlJob) {
            if(TransactionIDEnum.equals(objCommon.getTransactionID(), TransactionIDEnum.DURABLE_OPE_COMP_REQ)) {
                retVal = false;
            }
        } else {
            log.debug("aDurableControlJob is not null");
            //---------------------------------
            // CJ has a relation with Machine?
            //---------------------------------
            CimMachine aMachine = aDurableControlJob.getMachine();
            if (null == aMachine) {
                log.debug("aDurableControlJob->aMachine is nil. Control Job doesn't have a relation with Machine.");
                retVal = false;
            }
        }
        log.info("currentPOFlag : {}", retVal);
        return retVal;
    }

    @Override
    public void durableInPostProcessFlagSet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, boolean inPostProcessFlag) {
        Validations.check(null == objCommon || null == durableCategory || null == durableID, retCodeConfig.getInvalidInputParam());
        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundCassette());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticle());
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }
        assert null != aDurable;
        if (inPostProcessFlag) {
            aDurable.makePostProcessFlagOn();
        } else {
            aDurable.makePostProcessFlagOff();
        }
    }

    @Override
    public String durableStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        Validations.check(null == objCommon || null == durableCategory || null == durableID, retCodeConfig.getInvalidInputParam());
        String durableState = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == cassette, retCodeConfig.getNotFoundCassette());
                durableState = cassette.getDurableState();
                break;
            case SP_DURABLECAT_RETICLEPOD:
                CimReticlePod reticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == reticlePod, retCodeConfig.getNotFoundReticlePod());
                Boolean isAvailableFlag = reticlePod.isAvailable();
                Boolean isNotAvailableFlag = reticlePod.isNotAvailable();
                Boolean isInUseFlag = reticlePod.isInUse();
                Boolean isScrappedFlag = reticlePod.isScrapped();
                if (isAvailableFlag) {
                    durableState = BizConstant.CIMFW_DURABLE_AVAILABLE;
                } else if (isNotAvailableFlag) {
                    durableState = BizConstant.CIMFW_DURABLE_NOTAVAILABLE;
                } else if (isInUseFlag) {
                    durableState = BizConstant.CIMFW_DURABLE_INUSE;
                } else if (isScrappedFlag) {
                    durableState = BizConstant.CIMFW_DURABLE_SCRAPPED;
                } else {
                    durableState = BizConstant.CIMFW_DURABLE_UNDEFINED;
                }
                break;
            case SP_DURABLECAT_RETICLE:
                CimProcessDurable reticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == reticle, retCodeConfig.getNotFoundReticle());
                durableState = reticle.getDurableState();
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }
        return durableState;
    }

    @Override
    public List<Infos.DurableHoldHistory> durableHoldRelease(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID,
                                                             List<Infos.DurableHoldList> durableHoldLists, ObjectIdentifier releaseReasonCodeID) {
        List<Infos.DurableHoldHistory> retVal = new ArrayList<>();

        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());

        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundCassette());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticle());
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }
        assert null != aDurable;
        List<DurableDTO.PosDurableHoldRecord> aHoldRecordSequence = aDurable.allHoldRecords();

        int aHoldRecordsResponsibleCnt = 0;
        int aReleaseResponsibleCnt = 0;
        for (DurableDTO.PosDurableHoldRecord posDurableHoldRecord : aHoldRecordSequence) {
            if (posDurableHoldRecord.getResponsibleOperationFlag()) aHoldRecordsResponsibleCnt++;
        }

        log.info("aHoldRecordsResponsibleCnt == {}", aHoldRecordsResponsibleCnt);
        for (Infos.DurableHoldList durableHoldList : durableHoldLists) {
            DurableDTO.PosDurableHoldRecord aHoldRecordKey = new DurableDTO.PosDurableHoldRecord();
            aHoldRecordKey.setHoldType(durableHoldList.getHoldType());
            aHoldRecordKey.setReasonCode(durableHoldList.getHoldReasonCodeID());
            aHoldRecordKey.setHoldPerson(durableHoldList.getHoldUserID());
            aHoldRecordKey.setRelatedDurable(durableHoldList.getRelatedDurableID());

            DurableDTO.PosDurableHoldRecord aHoldRecordOut = aDurable.findHoldRecord(aHoldRecordKey);
            Validations.check(null == aHoldRecordOut || CimStringUtils.isEmpty(aHoldRecordOut.getHoldType()), retCodeConfig.getNotExistHold());

            if (aHoldRecordOut.getResponsibleOperationFlag()) aReleaseResponsibleCnt++;

            Infos.DurableHoldHistory holdHistory = new Infos.DurableHoldHistory();
            retVal.add(holdHistory);
            if (aHoldRecordsResponsibleCnt == 0) {
                holdHistory.setMovementFlag(false);
            } else {
                if (aHoldRecordOut.getResponsibleOperationFlag()) {
                    if (aReleaseResponsibleCnt == aHoldRecordsResponsibleCnt) {
                        holdHistory.setMovementFlag(true);
                    } else {
                        holdHistory.setMovementFlag(false);
                    }
                } else {
                    holdHistory.setMovementFlag(false);
                }
            }

            log.info("movementFlag : " + holdHistory.getMovementFlag());

            if (aHoldRecordsResponsibleCnt == 0) {
                holdHistory.setResponsibleOperationExistFlag(false);
            } else {
                if (aHoldRecordOut.getResponsibleOperationFlag()) {
                    holdHistory.setResponsibleOperationExistFlag(true);
                } else {
                    if (aReleaseResponsibleCnt == aHoldRecordsResponsibleCnt) {
                        holdHistory.setResponsibleOperationExistFlag(false);
                    } else {
                        holdHistory.setResponsibleOperationExistFlag(true);
                    }
                }
            }

            log.info("responsibleOperationExistFlag : " + holdHistory.getResponsibleOperationExistFlag());

            holdHistory.setChangeStateFlag(false);
            holdHistory.setHoldType(aHoldRecordOut.getHoldType());
            holdHistory.setHoldReasonCode(aHoldRecordOut.getReasonCode());
            holdHistory.setHoldPerson(aHoldRecordOut.getHoldPerson());
            holdHistory.setHoldTime(aHoldRecordOut.getHoldTimeStamp());
            holdHistory.setResponsibleOperationFlag(aHoldRecordOut.getResponsibleOperationFlag());
            holdHistory.setResponsibleRouteID(aHoldRecordOut.getResponsibleRouteID());
            holdHistory.setResponsibleOperationNumber(aHoldRecordOut.getResponsibleOperationNumber());
            holdHistory.setResponsibleOperationName(aHoldRecordOut.getResponsibleOperationName());
            holdHistory.setHoldClaimMemo(durableHoldList.getClaimMemo());
            holdHistory.setReleaseReasonCode(releaseReasonCodeID);
            holdHistory.setReleaseTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
            holdHistory.setReleasePerson(objCommon.getUser().getUserID());
            retVal.add(holdHistory);

            /*----------------------*/
            /*  Remove Hold Recode  */
            /*----------------------*/
            aDurable.removeHoldRecord(aHoldRecordKey);
        }

        int nDurHldLen = CimArrayUtils.getSize(durableHoldLists);
        int nHldRecLen = CimArrayUtils.getSize(aHoldRecordSequence);
        if (nDurHldLen == nHldRecLen && nDurHldLen > 0) {
            aDurable.makeNotOnHold();

            //--------------------------------------------------------------------------------------------
            //            Judgement of AddQue or MoveQue.
            // If releaseReasonCode is LOCR and Transaction is PostProcessExec or PostProcessActionUpdate,
            // this request is InternalHoldRelease for Post-Processing.
            // So, moveToWIPQueue is called.( HoldQue => WIPQue )
            //--------------------------------------------------------------------------------------------
            String transactionID = objCommon.getTransactionID();
            if (ObjectIdentifier.equalsWithValue(releaseReasonCodeID, BizConstant.SP_REASON_DURABLELOCKRELEASE) &&
                    (CimStringUtils.equals(transactionID, TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue())
                            || CimStringUtils.equals(transactionID, "OPOSW008")
                            || CimStringUtils.equals(transactionID, TransactionIDEnum.POST_PROCESS_ACTION_UPDATE.getValue()))) {
                //***************************************************************/
                /*  Check Hold Queue count.                                     */
                /*  If the Durable's Hold Queue doesn't exist, then this Hold   */
                /*  Release Request is required in OpeComp on Flow Batch Entry  */
                /*  Operation.                                                  */
                /*  If exists, then this request is on normal oepration.        */
                //***************************************************************/
                List<CimMachine> aPosMachineSequence = aDurable.getHoldQueuedMachines();
                if (CimArrayUtils.isNotEmpty(aPosMachineSequence)) {
                    //***************************************************************/
                    /*  "holdQueueCount = 1"                                        */
                    /*  ... It means this request is done in the OpeComp on normal  */
                    /*  Operation.                                                  */
                    //***************************************************************/
                    dispatchingManager.moveToDurableWIPQueue(durableCategory, durableID);
                }
            } else {
                dispatchingManager.removeFromDurableHoldQueue(durableCategory, durableID);

                dispatchingManager.addToDurableQueue(durableCategory, durableID);
            }


            aDurable.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aDurable.setStateChangedPerson(aPerson);
            retVal.get(0).setChangeStateFlag(true);
        } else if (nHldRecLen > nDurHldLen) {
            log.info("nHldRecLen > nDurHldLen");
            int bDurableHoldEqpUpdateFlag = StandardProperties.OM_EQP_HOLD_UPDATE_MODE.getIntValue();

            if (bDurableHoldEqpUpdateFlag == -1) {
                log.info("bDurableHoldEqpUpdateFlag == -1");
                List<DurableDTO.PosDurableHoldRecord> aResultHoldRecordSequence = aDurable.allHoldRecords();

                boolean bFindFlag = false;
                for (DurableDTO.PosDurableHoldRecord holdRecord : aResultHoldRecordSequence) {
                    if (!ObjectIdentifier.equalsWithValue(holdRecord.getReasonCode(), BizConstant.SP_REASON_DURABLELOCK)) {
                        log.info("It was found except the internal hold.");
                        bFindFlag = true;
                        break;
                    }
                }

                if (!bFindFlag) {
                    log.info("Only internal hold : FRXXX_HOLDEQP --> FRXXX_EQP");
                    dispatchingManager.removeFromDurableHoldQueue(durableCategory, durableID);

                    dispatchingManager.addToDurableQueue(durableCategory, durableID);
                }
            }
        }

        List<DurableDTO.PosDurableHoldRecord> anAfterHoldRecordSequence = aDurable.allHoldRecords();
        boolean aRespOpeExistFlag = false;
        for (DurableDTO.PosDurableHoldRecord durableHoldRecord : anAfterHoldRecordSequence) {
            if (durableHoldRecord.getResponsibleOperationFlag()) {
                aDurable.makeResponsibleOperation();
            }
            aRespOpeExistFlag = true;
            break;
        }

        if (!aRespOpeExistFlag) {
            aDurable.makeNotResponsibleOperation();
        }

        aDurable.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aDurable.setLastClaimedPerson(aPerson);
        return retVal;
    }

    @Override
    public void processCheckBranchCancelForDurable(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        CimProcessDefinition aProcessDefinition = null;
        CimDurableProcessFlowContext durablePFX = null;

        if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");

            //--------------------------------------------------------------------------------------------------
            // Get Cassette information
            //--------------------------------------------------------------------------------------------------
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aCassette), retCodeConfig.getNotFoundCassette(), durableID);

            //--------------------------------------------------------------------------------------------------
            // Check if Durable's current route is sub route
            //--------------------------------------------------------------------------------------------------
            aProcessDefinition = aCassette.getMainProcessDefinition();

            //--------------------------------------------------------------------------------------------------
            // Check if Durables' current operation is the first operation of the sub route
            //--------------------------------------------------------------------------------------------------
            durablePFX = aCassette.getDurableProcessFlowContext();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("durableCategory is ReticlePod");

            //--------------------------------------------------------------------------------------------------
            // Get ReticlePod information
            //--------------------------------------------------------------------------------------------------
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticlePod), retCodeConfig.getNotFoundReticlePod(), durableID);

            //--------------------------------------------------------------------------------------------------
            // Check if Durable's current route is sub route
            //--------------------------------------------------------------------------------------------------
            aProcessDefinition = aReticlePod.getMainProcessDefinition();

            //--------------------------------------------------------------------------------------------------
            // Check if Durables' current operation is the first operation of the sub route
            //--------------------------------------------------------------------------------------------------
            durablePFX = aReticlePod.getDurableProcessFlowContext();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("durableCategory is Reticle");

            //--------------------------------------------------------------------------------------------------
            // Get Reticle information
            //--------------------------------------------------------------------------------------------------
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticle), retCodeConfig.getNotFoundReticle(), durableID);

            //--------------------------------------------------------------------------------------------------
            // Check if Durable's current route is sub route
            //--------------------------------------------------------------------------------------------------
            aProcessDefinition = aReticle.getMainProcessDefinition();

            //--------------------------------------------------------------------------------------------------
            // Check if Durables' current operation is the first operation of the sub route
            //--------------------------------------------------------------------------------------------------
            durablePFX = aReticle.getDurableProcessFlowContext();
        }

        Validations.check(CimObjectUtils.isEmpty(aProcessDefinition), retCodeConfig.getSystemError());
        Validations.check(CimObjectUtils.isEmpty(durablePFX), retCodeConfig.getNotFoundPfx());

        String processFlowType = aProcessDefinition.getProcessFlowType();
        Validations.check(!CimStringUtils.equals(processFlowType, SP_FLOWTYPE_SUB), retCodeConfigEx.getDurableNotInSubroute());

        Boolean bFirstOperationFlag = durablePFX.isFirstOperationForProcessFlowOnCurrentRoute();
        Validations.check(CimBooleanUtils.isFalse(bFirstOperationFlag), retCodeConfig.getAllAlreadyProcessed());
    }

    @Override
    public void processDurableReworkCountDecrement(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aCassette), retCodeConfig.getNotFoundCassette(), durableID);

            //-------------------------------------------------------------------------------------
            // Make the key for the check of max rework count.
            // The key is the stringifiedObjectReference of previous ProcessOperationSpecification.
            //-------------------------------------------------------------------------------------
            aCassette.decreaseReworkCount();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("durableCategory is ReticlePod");
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticlePod), retCodeConfig.getNotFoundReticlePod(), durableID);

            //-------------------------------------------------------------------------------------
            // Make the key for the check of max rework count.
            // The key is the stringifiedObjectReference of previous ProcessOperationSpecification.
            //-------------------------------------------------------------------------------------
            aReticlePod.decreaseReworkCount();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("durableCategory is Reticle");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticle), retCodeConfig.getNotFoundReticle(), durableID);

            //-------------------------------------------------------------------------------------
            // Make the key for the check of max rework count.
            // The key is the stringifiedObjectReference of previous ProcessOperationSpecification.
            //-------------------------------------------------------------------------------------
            aReticle.decreaseReworkCount();
        }
    }

    @Override
    public Inputs.OldCurrentPOData processCancelBranchRouteForDurable(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        Inputs.OldCurrentPOData oldCurrentPOData = null;
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(CimObjectUtils.isEmpty(aPerson), retCodeConfig.getNotFoundPerson(), durableID);

        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aCassette), retCodeConfig.getNotFoundCassette(), durableID);

            CimDurableProcessOperation aDurablePO = aCassette.getDurableProcessOperation();
            Validations.check(CimObjectUtils.isEmpty(aDurablePO), retCodeConfig.getNotFoundDurablePo(), durableID);

            ProcessDTO.PosProcessOperationEventData eventData = aDurablePO.getEventData();
            oldCurrentPOData = new Inputs.OldCurrentPOData();
            oldCurrentPOData.setRouteID(eventData.getRouteID());
            oldCurrentPOData.setOperationNumber(eventData.getOperationNumber());
            oldCurrentPOData.setOperationID(eventData.getOperationID());
            oldCurrentPOData.setOperationPassCount(eventData.getOperationPassCount());
            oldCurrentPOData.setObjrefPOS(eventData.getObjrefPOS());
            oldCurrentPOData.setObjrefMainPF(eventData.getObjrefMainPF());
            oldCurrentPOData.setObjrefModulePOS(eventData.getObjrefModulePOS());

            //--------------------------------------------------------------------------------------------------
            // Get branch ProcessFlow ( Rework route or Sub route )
            //--------------------------------------------------------------------------------------------------
            CimProcessDefinition aProcessDefinition = aCassette.getMainProcessDefinition();
            Validations.check(CimObjectUtils.isEmpty(aProcessDefinition), retCodeConfig.getSystemError());

            //--------------------------------------------------------------------------------------------------
            // If branch route's pd type is "Rework", set rework out operation to output parameter.
            //--------------------------------------------------------------------------------------------------
            //===== Get and check branch route's pd type =====//
            String processDefinitionType = aProcessDefinition.getProcessDefinitionType();

            if (CimStringUtils.equals(processDefinitionType, SP_MAINPDTYPE_DURABLEREWORK)) {
                log.info("** Branch route's PD type is 'Rework'");
                //===== Get and set rework out operation from backup operation =====//
                CimDurableProcessFlowContext durablePFX = aCassette.getDurableProcessFlowContext();
                Validations.check(CimObjectUtils.isEmpty(durablePFX), retCodeConfig.getNotFoundPfx());

                ProcessDTO.BackupOperation backupOperation = durablePFX.getBackupOperation();
                if (!CimObjectUtils.isEmpty(backupOperation)) {
                    log.info("** Rework out operation is  {}", backupOperation.getReworkOutKey());
                    oldCurrentPOData.setReworkOutOperation(backupOperation.getReworkOutKey());
                }
            }

            //--------------------------------------------------------------------------------------------------
            // Remove durable from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.removeFromDurableHoldQueue(durableCategory, durableID);

            //--------------------------------------------------------------------------------------------------
            // Cancel Branch Route
            //--------------------------------------------------------------------------------------------------
            aCassette.cancelBranch();

            //--------------------------------------------------------------------------------------------------
            // Add durable into new current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(durableCategory, durableID);

            //--------------------------------------------------------------------------------------------------
            // Change durable production state.
            //--------------------------------------------------------------------------------------------------
            aCassette.changeProductionStateBy(objCommon.getTimeStamp().getReportTimeStamp(), aPerson);

            //--------------------------------------------------------------------------------------------------
            // Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aCassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aCassette.setLastClaimedPerson(aPerson);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticlePod), retCodeConfig.getNotFoundReticlePod(), durableID);

            CimDurableProcessOperation aDurablePO = aReticlePod.getDurableProcessOperation();
            Validations.check(CimObjectUtils.isEmpty(aDurablePO), retCodeConfig.getNotFoundDurablePo(), durableID);

            ProcessDTO.PosProcessOperationEventData eventData = aDurablePO.getEventData();
            oldCurrentPOData = new Inputs.OldCurrentPOData();
            oldCurrentPOData.setRouteID(eventData.getRouteID());
            oldCurrentPOData.setOperationNumber(eventData.getOperationNumber());
            oldCurrentPOData.setOperationID(eventData.getOperationID());
            oldCurrentPOData.setOperationPassCount(eventData.getOperationPassCount());
            oldCurrentPOData.setObjrefPOS(eventData.getObjrefPOS());
            oldCurrentPOData.setObjrefMainPF(eventData.getObjrefMainPF());
            oldCurrentPOData.setObjrefModulePOS(eventData.getObjrefModulePOS());

            //--------------------------------------------------------------------------------------------------
            // Get branch ProcessFlow ( Rework route or Sub route )
            //--------------------------------------------------------------------------------------------------
            CimProcessDefinition aProcessDefinition = aReticlePod.getMainProcessDefinition();
            Validations.check(CimObjectUtils.isEmpty(aProcessDefinition), retCodeConfig.getSystemError());

            //--------------------------------------------------------------------------------------------------
            // If branch route's pd type is "Rework", set rework out operation to output parameter.
            //--------------------------------------------------------------------------------------------------
            //===== Get and check branch route's pd type =====//
            String processDefinitionType = aProcessDefinition.getProcessDefinitionType();

            if (CimStringUtils.equals(processDefinitionType, SP_MAINPDTYPE_DURABLEREWORK)) {
                log.info("** Branch route's PD type is 'Rework'");
                //===== Get and set rework out operation from backup operation =====//
                CimDurableProcessFlowContext durablePFX = aReticlePod.getDurableProcessFlowContext();
                Validations.check(CimObjectUtils.isEmpty(durablePFX), retCodeConfig.getNotFoundPfx());

                ProcessDTO.BackupOperation backupOperation = durablePFX.getBackupOperation();
                if (!CimObjectUtils.isEmpty(backupOperation)) {
                    log.info("** Rework out operation is  {}", backupOperation.getReworkOutKey());
                    oldCurrentPOData.setReworkOutOperation(backupOperation.getReworkOutKey());
                }
            }

            //--------------------------------------------------------------------------------------------------
            // Remove durable from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.removeFromDurableHoldQueue(durableCategory, durableID);

            //--------------------------------------------------------------------------------------------------
            // Cancel Branch Route
            //--------------------------------------------------------------------------------------------------
            aReticlePod.cancelBranch();

            //--------------------------------------------------------------------------------------------------
            // Add durable into new current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(durableCategory, durableID);

            //--------------------------------------------------------------------------------------------------
            // Change durable production state.
            //--------------------------------------------------------------------------------------------------
            aReticlePod.changeProductionStateBy(objCommon.getTimeStamp().getReportTimeStamp(), aPerson);

            //--------------------------------------------------------------------------------------------------
            // Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aReticlePod.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aReticlePod.setLastClaimedPerson(aPerson);
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticle), retCodeConfig.getNotFoundReticle(), durableID);

            CimDurableProcessOperation aDurablePO = aReticle.getDurableProcessOperation();
            Validations.check(CimObjectUtils.isEmpty(aDurablePO), retCodeConfig.getNotFoundDurablePo(), durableID);

            ProcessDTO.PosProcessOperationEventData eventData = aDurablePO.getEventData();
            oldCurrentPOData = new Inputs.OldCurrentPOData();
            oldCurrentPOData.setRouteID(eventData.getRouteID());
            oldCurrentPOData.setOperationNumber(eventData.getOperationNumber());
            oldCurrentPOData.setOperationID(eventData.getOperationID());
            oldCurrentPOData.setOperationPassCount(eventData.getOperationPassCount());
            oldCurrentPOData.setObjrefPOS(eventData.getObjrefPOS());
            oldCurrentPOData.setObjrefMainPF(eventData.getObjrefMainPF());
            oldCurrentPOData.setObjrefModulePOS(eventData.getObjrefModulePOS());

            //--------------------------------------------------------------------------------------------------
            // Get branch ProcessFlow ( Rework route or Sub route )
            //--------------------------------------------------------------------------------------------------
            CimProcessDefinition aProcessDefinition = aReticle.getMainProcessDefinition();
            Validations.check(CimObjectUtils.isEmpty(aProcessDefinition), retCodeConfig.getSystemError());

            //--------------------------------------------------------------------------------------------------
            // If branch route's pd type is "Rework", set rework out operation to output parameter.
            //--------------------------------------------------------------------------------------------------
            //===== Get and check branch route's pd type =====//
            String processDefinitionType = aProcessDefinition.getProcessDefinitionType();

            if (CimStringUtils.equals(processDefinitionType, SP_MAINPDTYPE_DURABLEREWORK)) {
                log.info("** Branch route's PD type is 'Rework'");
                //===== Get and set rework out operation from backup operation =====//
                CimDurableProcessFlowContext durablePFX = aReticle.getDurableProcessFlowContext();
                Validations.check(CimObjectUtils.isEmpty(durablePFX), retCodeConfig.getNotFoundPfx());

                ProcessDTO.BackupOperation backupOperation = durablePFX.getBackupOperation();
                if (!CimObjectUtils.isEmpty(backupOperation)) {
                    log.info("** Rework out operation is  {}", backupOperation.getReworkOutKey());
                    oldCurrentPOData.setReworkOutOperation(backupOperation.getReworkOutKey());
                }
            }

            //--------------------------------------------------------------------------------------------------
            // Remove durable from current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.removeFromDurableHoldQueue(durableCategory, durableID);

            //--------------------------------------------------------------------------------------------------
            // Cancel Branch Route
            //--------------------------------------------------------------------------------------------------
            aReticle.cancelBranch();

            //--------------------------------------------------------------------------------------------------
            // Add durable into new current dispatching queue
            //--------------------------------------------------------------------------------------------------
            dispatchingManager.addToDurableQueue(durableCategory, durableID);

            //--------------------------------------------------------------------------------------------------
            // Change durable production state.
            //--------------------------------------------------------------------------------------------------
            aReticle.changeProductionStateBy(objCommon.getTimeStamp().getReportTimeStamp(), aPerson);

            //--------------------------------------------------------------------------------------------------
            // Set Last Claim Time and Last Claim Person
            //--------------------------------------------------------------------------------------------------
            aReticle.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aReticle.setLastClaimedPerson(aPerson);
        }
        return oldCurrentPOData;
    }

    @Override
    public String durableProductionStateGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        String durableProductionState = null;
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aCassette), retCodeConfig.getNotFoundCassette(), durableID);
            durableProductionState = aCassette.getDurableProductionState();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("durableCategory is ReticlePod");
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticlePod), retCodeConfig.getNotFoundReticlePod(), durableID);
            durableProductionState = aReticlePod.getDurableProductionState();
        } else if (CimStringUtils.equals(durableCategory, BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("durableCategory is Reticle");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            Validations.check(CimObjectUtils.isEmpty(aReticle), retCodeConfig.getNotFoundReticle(), durableID);
            durableProductionState = aReticle.getDurableProductionState();
        }
        return durableProductionState;
    }

    @Override
    public List<Infos.DurableHoldHistory> durableHold(Infos.ObjCommon objCommon, Infos.DurableHoldIn paramIn) {
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        List<Infos.DurableHoldHistory> retVal = new ArrayList<>();

        boolean bChangeStateFlag = false;
        boolean bOnHoldFlag = false;
        boolean bDurableLockFlag = false;
        boolean bRespOpeExistFlag = false;
        int nDurableHoldEqpUpdateFlag = StandardProperties.OM_EQP_HOLD_UPDATE_MODE.getIntValue();
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());

        List<Infos.DurableHoldList> durableHoldList = paramIn.getStrDurableHoldList();
        //-------------------------------------------
        // Check input Parameters
        //-------------------------------------------
        Validations.check(CimArrayUtils.isEmpty(durableHoldList), retCodeConfig.getInvalidParameter());
        String durableCategory = paramIn.getDurableCategory();
        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                log.info("durableCategory is Cassette");
                aDurable = baseCoreFactory.getBO(CimCassette.class, paramIn.getDurableID());
                Validations.check(null == aDurable, retCodeConfig.getNotFoundCassette());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                log.info("durableCategory is ReticlePod");
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, paramIn.getDurableID());
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                break;
            case SP_DURABLECAT_RETICLE:
                log.info("durableCategory is Reticle");
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, paramIn.getDurableID());
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticle());
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }
        assert null != aDurable;

        // Get Durable Hold State
        bOnHoldFlag = aDurable.isOnHold();
        if (nDurableHoldEqpUpdateFlag == -1) {
            for (Infos.DurableHoldList holdList : durableHoldList) {
                if (ObjectIdentifier.equalsWithValue(BizConstant.SP_REASON_DURABLELOCK, holdList.getHoldReasonCodeID())) {
                    bDurableLockFlag = true;
                    break;
                }
            }
        }

        if (nDurableHoldEqpUpdateFlag == 1 || !bDurableLockFlag) {
            dispatchingManager.addToDurableHoldQueue(durableCategory, paramIn.getDurableID());
        }

        if (!bOnHoldFlag) {
            aDurable.makeOnHold();
            aDurable.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aDurable.setStateChangedPerson(aPerson);
            bChangeStateFlag = true;
        }

        if (nDurableHoldEqpUpdateFlag == 1 || !bDurableLockFlag) {
            dispatchingManager.removeFromDurableQueue(durableCategory, paramIn.getDurableID());
        }

        aDurable.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aDurable.setLastClaimedPerson(aPerson);

        List<DurableDTO.PosDurableHoldRecord> posDurableHoldRecords = aDurable.allHoldRecords();
        for (DurableDTO.PosDurableHoldRecord posDurableHoldRecord : posDurableHoldRecords) {
            if (posDurableHoldRecord.getResponsibleOperationFlag()) {
                bRespOpeExistFlag = true;
                break;
            }
        }

        int loopIndex = 0;
        for (Infos.DurableHoldList holdList : durableHoldList) {
            boolean bPreviousFlag = false;
            CimDurableProcessOperation aPosPO = null;

            DurableDTO.PosDurableHoldRecord aDurableHoldRecord = new DurableDTO.PosDurableHoldRecord();
            //---------------------
            // Get Durable PO
            //---------------------
            if (CimStringUtils.equals(holdList.getResponsibleOperationMark(), SP_RESPONSIBLEOPERATION_PREVIOUS)) {
                aDurable.makeResponsibleOperation();
                aPosPO = aDurable.getPreviousDurableProcessOperation();
                bPreviousFlag = true;
            } else {
                aPosPO = aDurable.getDurableProcessOperation();
            }
            Validations.check(null == aPosPO, retCodeConfig.getNotFoundPoForDurable(),"*****",ObjectIdentifier.fetchValue(paramIn.getDurableID()));

            // -- Main PD
            CimProcessDefinition aMainPD = aPosPO.getMainProcessDefinition();
            Validations.check(null == aMainPD, retCodeConfig.getNotFoundRoute());

            aDurableHoldRecord.setResponsibleRouteID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
            aDurableHoldRecord.setResponsibleOperationNumber(aPosPO.getOperationNumber());
            aDurableHoldRecord.setResponsibleOperationName(aPosPO.getProcessDefinitionName());

            // -- Hold Type
            if (CimStringUtils.isNotEmpty(holdList.getHoldType())) {
                aDurableHoldRecord.setHoldType(holdList.getHoldType());
            } else {
                aDurableHoldRecord.setHoldType(BizConstant.SP_HOLDTYPE_DURABLEHOLD);
            }

            // -- reasonCode
            aDurableHoldRecord.setReasonCode(holdList.getHoldReasonCodeID());

            // -- hold user
            if (ObjectIdentifier.isNotEmptyWithValue(holdList.getHoldUserID())) {
                aDurableHoldRecord.setHoldPerson(holdList.getHoldUserID());
            } else {
                aDurableHoldRecord.setHoldPerson(objCommon.getUser().getUserID());
            }

            aDurableHoldRecord.setHoldTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            aDurableHoldRecord.setRelatedDurable(holdList.getRelatedDurableID());
            aDurableHoldRecord.setResponsibleOperationFlag(bPreviousFlag);
            aDurableHoldRecord.setHoldClaimMemo(holdList.getClaimMemo());

            //--------------------------------//
            // Add Hold Record for Durable    //
            //--------------------------------//
            aDurable.addHoldRecord(aDurableHoldRecord);

            //----------------------
            // Make History
            //----------------------
            Infos.DurableHoldHistory history = new Infos.DurableHoldHistory();
            if (!bRespOpeExistFlag && bPreviousFlag) {
                history.setMovementFlag(true);
            } else {
                history.setMovementFlag(false);
            }

            history.setResponsibleOperationExistFlag(bRespOpeExistFlag);
            if (bChangeStateFlag && loopIndex == 0) {
                history.setChangeStateFlag(true);
            } else {
                history.setChangeStateFlag(false);
            }

            history.setHoldType(aDurableHoldRecord.getHoldType());
            history.setHoldReasonCode(aDurableHoldRecord.getReasonCode());
            history.setHoldPerson(aDurableHoldRecord.getHoldPerson());
            history.setHoldTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
            history.setResponsibleOperationFlag(bPreviousFlag);
            history.setResponsibleRouteID(aDurableHoldRecord.getResponsibleRouteID());
            history.setResponsibleOperationNumber(aDurableHoldRecord.getResponsibleOperationNumber());
            history.setResponsibleOperationName(aDurableHoldRecord.getResponsibleOperationName());
            history.setHoldClaimMemo(aDurableHoldRecord.getHoldClaimMemo());

            retVal.add(history);

            if (bPreviousFlag) {
                bRespOpeExistFlag = true;
            }
            loopIndex++;
        }

        List<DurableDTO.PosDurableHoldRecord> anAfterHoldRecordSequence = aDurable.allHoldRecords();
        bRespOpeExistFlag = false;
        for (DurableDTO.PosDurableHoldRecord durableHoldRecord : anAfterHoldRecordSequence) {
            if (durableHoldRecord.getResponsibleOperationFlag()) {
                aDurable.makeResponsibleOperation();
                bRespOpeExistFlag = true;
                break;
            }
        }

        if (!bRespOpeExistFlag) {
            aDurable.makeNotResponsibleOperation();
        }
        return retVal;
    }

    @Override
    public boolean durableCheckConditionForAutoBankIn(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableCheckConditionForAutoBankInIn strDurableCheckConditionForAutoBankInin) {

        log.info("PPTManager_i::durable_CheckConditionForAutoBankIn");

        boolean strDurableCheckConditionForAutoBankInout = false;

        CimDurableProcessOperation aDurablePO;
        boolean autoBankInFlag = false;
        boolean bankInRequiredFlag = false;

        if (CimStringUtils.equals(strDurableCheckConditionForAutoBankInin.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strDurableCheckConditionForAutoBankInin.getDurableID());

            aDurablePO = aCassette.getDurableProcessOperation();

            if (aDurablePO != null) {
                log.info("{}", "aDurablePO is not nil");
                autoBankInFlag = aDurablePO.isAutoBankInRequired();
            }
            log.info("{} {}", "Durable PO's autoBankInFlag = ", (autoBankInFlag ? "TRUE" : "FALSE"));

            if (autoBankInFlag) {
                bankInRequiredFlag = aCassette.isBankInRequired();
            }
            log.info("{} {}", "Cassette's bankInRequiredFlag = ", (bankInRequiredFlag ? "TRUE" : "FALSE"));

            if (autoBankInFlag &&
                    bankInRequiredFlag) {
                log.info("{}", "autoBankInFlag is TRUE and bankInRequiredFlag is TRUE");
                strDurableCheckConditionForAutoBankInout = true;
            } else {
                strDurableCheckConditionForAutoBankInout = false;
            }
        } else if (CimStringUtils.equals(strDurableCheckConditionForAutoBankInin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strDurableCheckConditionForAutoBankInin.getDurableID());

            aDurablePO = aReticlePod.getDurableProcessOperation();

            if (aDurablePO != null) {
                log.info("{}", "aDurablePO is not nil");
                autoBankInFlag = aDurablePO.isAutoBankInRequired();
            }
            log.info("{} {}", "Durable PO's autoBankInFlag = ", (autoBankInFlag ? "TRUE" : "FALSE"));

            if (autoBankInFlag) {
                bankInRequiredFlag = aReticlePod.isBankInRequired();
            }
            log.info("{} {}", "ReticlePod's bankInRequiredFlag = ", (bankInRequiredFlag ? "TRUE" : "FALSE"));

            if (autoBankInFlag &&
                    bankInRequiredFlag) {
                log.info("{}", "autoBankInFlag is TRUE and bankInRequiredFlag is TRUE");
                strDurableCheckConditionForAutoBankInout = true;
            } else {
                strDurableCheckConditionForAutoBankInout = false;
            }
        } else if (CimStringUtils.equals(strDurableCheckConditionForAutoBankInin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strDurableCheckConditionForAutoBankInin.getDurableID());

            aDurablePO = aReticle.getDurableProcessOperation();

            if (aDurablePO != null) {
                log.info("{}", "aDurablePO is not nil");
                autoBankInFlag = aDurablePO.isAutoBankInRequired();
            }
            log.info("{} {}", "Durable PO's autoBankInFlag = ", (autoBankInFlag ? "TRUE" : "FALSE"));

            if (autoBankInFlag) {
                bankInRequiredFlag = aReticle.isBankInRequired();
            }
            log.info("{} {}", "Reticle's bankInRequiredFlag = ", (bankInRequiredFlag ? "TRUE" : "FALSE"));

            if (autoBankInFlag &&
                    bankInRequiredFlag) {
                log.info("{}", "autoBankInFlag is TRUE and bankInRequiredFlag is TRUE");
                strDurableCheckConditionForAutoBankInout = true;
            } else {
                strDurableCheckConditionForAutoBankInout = false;
            }
        }

        log.info("PPTManager_i::durable_CheckConditionForAutoBankIn");
        return strDurableCheckConditionForAutoBankInout;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableoriginalRouteListGetin
     * @return com.fa.cim.dto.Infos.DurableOriginalRouteListGetOut
     * @throws
     * @author ho
     * @date 2020/7/13 9:47
     */
    public Infos.DurableOriginalRouteListGetOut durableOriginalRouteListGet(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableOriginalRouteListGetIn strDurableoriginalRouteListGetin) {
        Infos.DurableOriginalRouteListGetOut strDurableoriginalRouteListGetout = new Infos.DurableOriginalRouteListGetOut();
        log.info("PPTManager_i::durable_originalRouteList_Get");

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        CimDurableProcessFlowContext durablePFX = null;

        /*---------------------------*/
        /* Get Process Flow Context  */
        /*---------------------------*/
        if (CimStringUtils.equals(strDurableoriginalRouteListGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strDurableoriginalRouteListGetin.getDurableID());

            durablePFX = aCassette.getDurableProcessFlowContext();
        } else if (CimStringUtils.equals(strDurableoriginalRouteListGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strDurableoriginalRouteListGetin.getDurableID());

            durablePFX = aReticlePod.getDurableProcessFlowContext();
        } else if (CimStringUtils.equals(strDurableoriginalRouteListGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strDurableoriginalRouteListGetin.getDurableID());

            durablePFX = aReticle.getDurableProcessFlowContext();
        }

        if (durablePFX == null) {
            log.info("{}", "durablePFX is nil");
            Validations.check(true,
                    retCodeConfig.getNotFoundPfx(), "");
        }

        /*------------------------------------------*/
        /*   Check Durable is on Main or Sub Route  */
        /*   if return no is 0, MainRoute           */
        /*   if return no >0, SubRoute              */
        /*------------------------------------------*/
        log.info("PPTManager_i::durable_originalRouteList_Get", "durablePFX->allReturnOperations()");
        List<ProcessDTO.ReturnOperation> aReturnOperationSequence = null;
        List<ProcessDTO.ReturnOperation> aTmpROSeq;
        aReturnOperationSequence = durablePFX.allReturnOperations();
        aTmpROSeq = aReturnOperationSequence;

        int aReturnOperationSequenceLen = CimArrayUtils.getSize(aReturnOperationSequence);
        log.info("{} {} {}", "PPTManager_i::durable_originalRouteList_Get",
                "aReturnOperationSequenceLen ", aReturnOperationSequenceLen);

        /* -------------------------------------- */
        /* On SubRoute.                           */
        /* ( aReturnOperationSequenceLen > 0 )    */
        /* -------------------------------------- */
        if (aReturnOperationSequenceLen > 0) {
            /* -------------------------------------- */
            /* Get original route sequece data        */
            /* -------------------------------------- */
            strDurableoriginalRouteListGetout.setOriginalRouteID(new ArrayList<>(aReturnOperationSequenceLen));
            strDurableoriginalRouteListGetout.setReturnOperationNumber(new ArrayList<>(aReturnOperationSequenceLen));
            for (int i = 0; i < aReturnOperationSequenceLen; i++) {

                log.info("PPTManager_i::durable_originalRouteList_Get {} {}", "for loop to Get original route sequence data.", i);

                /*------------------------------------------*/
                /*   Get Process Flow of original Route ID  */
                /*------------------------------------------*/
                log.info("PPTManager_i::durable_originalRouteList_Get {}", "aPFX->getReturnOperationFor()");

                ProcessDTO.ReturnOperation strPosReturnOperation;
                ProcessDTO.ReturnOperation strPosReturnOperationVar;
                strPosReturnOperation = durablePFX.getReturnOperationFor(i);
                strPosReturnOperationVar = strPosReturnOperation;

                /*---------------------------------------*/
                /*   Get returnOperationNumber           */
                /*---------------------------------------*/
                strDurableoriginalRouteListGetout.getReturnOperationNumber().add(strPosReturnOperation.getOperationNumber());
                log.info("{} {} {}", "PPTManager_i:: durable_originalRouteList_Get",
                        "strDurableoriginalRouteListGetout.returnOperationNumber[i] ", strDurableoriginalRouteListGetout.getReturnOperationNumber().get(i));

                /*---------------------------------------*/
                /*   Get Original Process Definition ID  */
                /*---------------------------------------*/
                log.info("PPTManager_i::durable_originalRouteList_Get {}", "aPF->getRootProcessDefinition()");

                CimProcessFlow aPF;
                aPF = baseCoreFactory.getBO(CimProcessFlow.class, strPosReturnOperation.getProcessFlow());

                if (aPF == null) {
                    log.info("PPTManager_i:: durable_originalRouteList_Get", "CORBA::is_nil( aPF ) == TRUE");
                    Validations.check(true, retCodeConfig.getNotFoundPfForDurable(), ObjectIdentifier.fetchValue(strDurableoriginalRouteListGetin.getDurableID()));
                }

                CimProcessDefinition originalRoutePD;
                originalRoutePD = aPF.getRootProcessDefinition();

                if (originalRoutePD == null) {
                    log.info("PPTManager_i::durable_originalRouteList_Get", "originalRoutePD is NIL.");
                    Validations.check(true, retCodeConfig.getNotFoundProcessDefinition(), "");
                }

                strDurableoriginalRouteListGetout.getOriginalRouteID().add(
                        ObjectIdentifier.build(originalRoutePD.getIdentifier(), originalRoutePD.getPrimaryKey()));


            }
        } else {
            /* -------------------------------------- */
            /* On MainRoute.                          */
            /* -------------------------------------- */
            log.info("PPTManager_i::durable_originalRouteList_Get", "aReturnOperationSequenceLen <= 0");
            strDurableoriginalRouteListGetout.setOriginalRouteID(new ArrayList<>(0));
            strDurableoriginalRouteListGetout.setReturnOperationNumber(new ArrayList<>(0));
        }

        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        log.info("PPTManager_i::durable_originalRouteList_Get");
        return strDurableoriginalRouteListGetout;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurablerouteIDGetin
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @throws
     * @author ho
     * @date 2020/7/13 14:12
     */
    public ObjectIdentifier durableRouteIDGet(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableRouteIDGetIn strDurablerouteIDGetin) {
        log.info("PPTManager_i::durable_routeID_Get");
        CimProcessDefinition aMainPD = null;

        if (CimStringUtils.equals(strDurablerouteIDGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE)) {
            log.info("{}", "durableCategory is Cassette");
            CimCassette aCassette;
            aCassette = baseCoreFactory.getBO(CimCassette.class,
                    strDurablerouteIDGetin.getDurableID());

            // Get Durables' Main PD
            aMainPD = aCassette.getMainProcessDefinition();
        } else if (CimStringUtils.equals(strDurablerouteIDGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            log.info("{}", "durableCategory is ReticlePod");
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strDurablerouteIDGetin.getDurableID());

            // Get Durables' Main PD
            aMainPD = aReticlePod.getMainProcessDefinition();
        } else if (CimStringUtils.equals(strDurablerouteIDGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE)) {
            log.info("{}", "durableCategory is Reticle");
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strDurablerouteIDGetin.getDurableID());

            // Get Durables' Main PD
            aMainPD = aReticle.getMainProcessDefinition();
        }

        if (aMainPD == null) {
            log.info("{}", "##### aMainPD is Nil");
            Validations.check(true, retCodeConfig.getNotFoundRoute(), "");
        }

        log.info("PPTManager_i::durable_routeID_Get");
        return ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strDurableCheckLockHoldConditionForOperationin
     * @return void
     * @throws
     * @author ho
     * @date 2020/7/17 15:56
     */
    public void durableCheckLockHoldConditionForOperation(
            Infos.ObjCommon strObjCommonIn,
            Infos.DurableCheckLockHoldConditionForOperationIn strDurableCheckLockHoldConditionForOperationin) {
        log.info("PPTManager_i::durable_CheckLockHoldConditionForOperation");

        int durableLen = CimArrayUtils.getSize(strDurableCheckLockHoldConditionForOperationin.getDurableIDSeq());
        if (0 == durableLen) {
            log.info("{}", "The in-parameter is invalid.");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        log.info("{} {}", "durableIDSeq.length()", durableLen);
        for (int durableSeq = 0; durableSeq < durableLen; durableSeq++) {
            //------------------------------
            // Get the durables' hold list.
            //------------------------------

            List<Infos.DurableHoldListAttributes> strDurableFillInODRBQ019out = null;
            try {
                strDurableFillInODRBQ019out = durableFillInODRBQ019(strObjCommonIn, strDurableCheckLockHoldConditionForOperationin.getDurableCategorySeq().get(durableSeq),
                        strDurableCheckLockHoldConditionForOperationin.getDurableIDSeq().get(durableSeq));
            } catch (ServiceException ex) {
                if (!Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundEntry())) {
                    throw ex;
                }
            }


            log.info("{}", "durable_FillInODRBQ019() == RC_OK");

            //---------------------------
            // Check LOCK Hold.
            // Check WDCH Hold.
            //---------------------------
            int drblRsnLen = CimArrayUtils.getSize(strDurableFillInODRBQ019out);
            log.info("{} {}", "strDurableFillInODRBQ019out.strDurableHoldListAttributes.length", drblRsnLen);

            for (int drblRsnSeq = 0; drblRsnSeq < drblRsnLen; drblRsnSeq++) {
                if (strDurableFillInODRBQ019out != null) {
                    if (CimStringUtils.equals(ObjectIdentifier.fetchValue(strDurableFillInODRBQ019out.get(drblRsnSeq).getReasonCodeID()), BizConstant.SP_REASON_DURABLELOCK)) {
                        log.info("{}", "Find LOCK Hold durable. Cannot perform the operation... ");
                        Validations.check(true,
                                retCodeConfigEx.getPostprocLockholdForDurable(),
                                ObjectIdentifier.fetchValue(strDurableCheckLockHoldConditionForOperationin.getDurableIDSeq().get(durableSeq)));
                    }

                    if (CimStringUtils.equals(ObjectIdentifier.fetchValue(strDurableFillInODRBQ019out.get(drblRsnSeq).getReasonCodeID()), BizConstant.SP_REASON_WAITINGFORDATACOLLECTIONHOLD)) {
                        log.info("{}", "Find WDCH Hold durable. Cannot perform the operation... ");
                        Validations.check(true,
                                retCodeConfigEx.getWaitingForDataCollectionForDurable(),
                                ObjectIdentifier.fetchValue(strDurableCheckLockHoldConditionForOperationin.getDurableIDSeq().get(durableSeq)));
                    }
                }
            }
        }

        log.info("PPTManager_i::durable_CheckLockHoldConditionForOperation");
    }

    @Override
    public String durableDurableCategoryGet(Infos.ObjCommon objCommon, ObjectIdentifier durableID) {
        Validations.check(null == objCommon || null == durableID, retCodeConfig.getInvalidInputParam());
        Durable aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
        if(null != aDurable) {
            return BizConstant.SP_DURABLECAT_CASSETTE;
        }
        aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
        if(null != aDurable) {
            return BizConstant.SP_DURABLECAT_RETICLEPOD;
        }

        aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
        if(null != aDurable) {
            return BizConstant.SP_DURABLECAT_RETICLE;
        }
        return null;
    }

    @Override
    public Results.CandidateDurableJobStatusDetail durableCandidateJobStatusGet(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID) {
        Validations.check(null == objCommon || null == durableCategory || null == durableID, retCodeConfig.getInvalidInputParam());
        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundCassette());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticle());
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }
        assert null != aDurable;
        CimDurableProcessOperation durablePO = aDurable.getDurableProcessOperation();
        Validations.check(null == durablePO, retCodeConfig.getNotFoundDurablePo());
        CimProcessDefinition step = durablePO.getProcessDefinition();
        Validations.check(null == step, retCodeConfig.getNotFoundProcessDefinition());

        List<String> jobStatuses = step.allJobStatus();
        String currentJobStatus = aDurable.getJobStatus();
        List<String> jobStatusResult = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(jobStatuses)) {
            for (String jobStatus : jobStatuses) {
                if (null != currentJobStatus && CimStringUtils.equals(jobStatus, currentJobStatus)) {
                    continue;
                }
                jobStatusResult.add(jobStatus);
            }
        }

        Results.CandidateDurableJobStatusDetail retVal = new Results.CandidateDurableJobStatusDetail();
        retVal.setDurableStatus(aDurable.getDurableState());
        retVal.setJobStatus(jobStatusResult);
        return retVal;
    }

    @Override
    public void durableJobStateCheck(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, String jobStatus) {
        Validations.check(null == objCommon || null == durableCategory || null == durableID || null == jobStatus, retCodeConfig.getInvalidInputParam());
        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundCassette());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticle());
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }
        assert null != aDurable;
        String currentJobStatus = aDurable.getJobStatus();
        if (null != currentJobStatus && CimStringUtils.equals(currentJobStatus, jobStatus)) {
            Validations.check(retCodeConfigEx.getDurableJobStatusChangeFail(), currentJobStatus, jobStatus);
        }
    }

    @Override
    public void durableJobStateChange(Infos.ObjCommon objCommon, String durableCategory, ObjectIdentifier durableID, String jobStatus) {
        Validations.check(null == objCommon || null == durableCategory || null == durableID || null == jobStatus, retCodeConfig.getInvalidInputParam());
        Durable aDurable = null;
        switch (durableCategory) {
            case SP_DURABLECAT_CASSETTE:
                aDurable = baseCoreFactory.getBO(CimCassette.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundCassette());
                break;
            case SP_DURABLECAT_RETICLEPOD:
                aDurable = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticlePod());
                break;
            case SP_DURABLECAT_RETICLE:
                aDurable = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                Validations.check(null == aDurable, retCodeConfig.getNotFoundReticle());
                break;
            default:
                Validations.check(retCodeConfig.getInvalidDurableCategory());
                break;
        }
        assert null != aDurable;

        // Change Job status
        aDurable.setJobStatus(jobStatus);
        aDurable.setJobStatusChangeTime(CimDateUtils.getCurrentTimeStamp());
    }

    @Override
    public void durableCheckConditionForMachineAndChamber(Infos.ObjCommon objCommon, String equipmentID, String chamberID) {
        Validations.check(null == objCommon || null == equipmentID, retCodeConfig.getInvalidInputParam());
        CimMachine aMachine = baseCoreFactory.getBOByIdentifier(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundMachine());

        //---------------------------------//
        //  Check ProcessResource          //
        //---------------------------------//
        List<ProcessResource> chambers = aMachine.allProcessResources();
        boolean foundFlag = false;
        if(CimArrayUtils.isNotEmpty(chambers)) {
            for (ProcessResource chamber : chambers) {
                if(CimStringUtils.equals(chamber.getIdentifier(), chamberID)) {
                    foundFlag = true;
                    break;
                }
            }
        }
        Validations.check(!foundFlag, retCodeConfig.getNotFoundChamber());

    }

    @Override
    public Outputs.ObjMultiDurableXferFillInOTMSW005InParmOut multiDurableXferFillInOTMSW005InParm(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.StartDurable> strStartDurables, String durableCategory) {
        //Initialize
        Outputs.ObjMultiDurableXferFillInOTMSW005InParmOut out  = new Outputs.ObjMultiDurableXferFillInOTMSW005InParmOut();
        List<Infos.DurableXferReq> strDurableXferReq = new ArrayList<>();
        int nLen = CimArrayUtils.getSize(strStartDurables);
        for (int i = 0; i < nLen; i++) {
            Infos.DurableXferReq durableXferReq = new Infos.DurableXferReq();
            strDurableXferReq.add(durableXferReq);
            durableXferReq.setN2PurgeFlag(false);
            durableXferReq.setMandatoryFlag(false);
            log.info("fromMachineID: Get Current Stocker Object By Assigned Cassette Object");
            //fromMachineID: Get Current Stocker Object By Assigned Cassette Object
            if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE,durableCategory)){
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, strStartDurables.get(i).getDurableId());
                Machine aMachine = aCassette.currentAssignedMachine();
                String xferStat = aCassette.getTransportState();

                Validations.check(null == aMachine, new OmCode(retCodeConfig.getInvalidCassetteTransferState(),strStartDurables.get(i).getDurableId().getValue()));
                if (!CimStringUtils.equals(xferStat,BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)){
                    String identifier = aMachine.getIdentifier();
                    strDurableXferReq.get(i).setFromMachineID(ObjectIdentifier.build(identifier, ""));
                    Boolean bStorageFlag  = aMachine.isStorageMachine();
                    if (CimBooleanUtils.isFalse(bStorageFlag)){
                        CimMachine aEquipment = (CimMachine) aMachine;
                        String equCategory = aEquipment.getCategory();
                        Infos.EqpPortInfo strEqpPortInfo = new Infos.EqpPortInfo();
                        if (CimStringUtils.equals(equCategory,BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)){
                            log.info("call equipment_portInfoForInternalBuffer_GetDR()");
                            //【step1】 - equipment_portInfoForInternalBuffer_GetDR
                            strEqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommonIn, strDurableXferReq.get(i).getFromMachineID());
                        }else {
                            log.info("call equipment_portInfo_GetDR()");
                            //【step2】 - equipment_portInfo_GetDR
                            strEqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommonIn, strDurableXferReq.get(i).getFromMachineID());
                        }
                        int lenPort = CimArrayUtils.getSize(strEqpPortInfo.getEqpPortStatuses());
                        for (int j = 0; j < lenPort; j++) {
                            if (ObjectIdentifier.equalsWithValue(strEqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID(),
                                    strStartDurables.get(i).getDurableId())){
                                strDurableXferReq.get(i).setFromPortID(strEqpPortInfo.getEqpPortStatuses().get(j).getPortID());
                                break;
                            }
                        }

                        Validations.check(ObjectIdentifier.isEmpty(strDurableXferReq.get(i).getFromPortID()) || ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(i).getFromPortID())
                                , retCodeConfig.getNotFoundPort());
                    }
                }
            }
            // TODO: 2020/9/15 durable
            // TODO: 2020/9/15 reticlPod

            log.info("set durableID");
            //carrierID
            strDurableXferReq.get(i).setDurableID(strStartDurables.get(i).getDurableId());
            log.info("zoneType: Get By Cassette");
            //zoneType: Get By Cassette
            //【step3】 - cassette_zoneType_Get
            if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE,durableCategory)){
                Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOutRetCode = cassetteMethod.cassetteZoneTypeGet(objCommonIn, strDurableXferReq.get(i).getDurableID());
                strDurableXferReq.get(i).setZoneType(cassetteZoneTypeGetOutRetCode.getZoneType());
                strDurableXferReq.get(i).setPriority(cassetteZoneTypeGetOutRetCode.getPriority());
            }else {
                //durable and reticl Pod
                // TODO: 2020/9/15
                strDurableXferReq.get(i).setZoneType(null);
                strDurableXferReq.get(i).setPriority(null);
            }

            log.info("strToMachine.toPortID: Set Load PortID");
            //strToMachine.toPortID: Set Load PortID
            List<Infos.ToMachine> strToMachine = new ArrayList<>();
            strDurableXferReq.get(i).setStrToMachine(strToMachine);
            Infos.ToMachine toMachine = new Infos.ToMachine();
            strToMachine.add(toMachine);
            toMachine.setToMachineID(equipmentID);
            toMachine.setToPortID(strStartDurables.get(i).getStartDurablePort().getLoadPortID());
            log.info("expectedEndTime: Set 1 Year After");
            //expectedEndTime: Set 1 Year After
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            long time = currentTime.getTime();
            Calendar ca = Calendar.getInstance();
            ca.add(Calendar.YEAR,1);
            //ca.add(Calendar.MONTH,1);
            Timestamp scTime = new Timestamp(ca.get(Calendar.YEAR) - 1900,
                    ca.get(Calendar.MONTH),
                    ca.get(Calendar.DATE),
                    ca.get(Calendar.HOUR),
                    ca.get(Calendar.MINUTE),
                    ca.get(Calendar.SECOND), 0);
            strDurableXferReq.get(i).setExpectedEndTime(scTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        out.setStrDurableXferReq(strDurableXferReq);
        return out;
    }

    @Override
    public Outputs.ObjDurableDeliveryRTDInterfaceReqOut durableDeliveryRTDInterfaceReq(Infos.ObjCommon objCommonIn, String kind, ObjectIdentifier keyID) {
        // TODO: 2020/9/15
        Outputs.ObjDurableDeliveryRTDInterfaceReqOut out = null;
        //return out;
        //test throw not found RTD
        throw new ServiceException(retCodeConfigEx.getNotFoundRTD());
    }

    @Override
    public Outputs.ObjSingleDurableXferFillInOTMSW006InParmOut singleDurableXferFillInOTMSW006InParm(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier unLoadPortID, ObjectIdentifier durableID, Results.DurableWhereNextStockerInqResult strWhereNextStockerInqResult,String durableCategory) {

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Outputs.ObjSingleDurableXferFillInOTMSW006InParmOut out = new Outputs.ObjSingleDurableXferFillInOTMSW006InParmOut();

        out.setRerouteFlag(false);
        Infos.DurableXferReq strDurableXferReq = new Infos.DurableXferReq();
        out.setStrDurableXferReq(strDurableXferReq);
        strDurableXferReq.setFromMachineID(equipmentID);
        strDurableXferReq.setFromPortID(unLoadPortID);
        strDurableXferReq.setDurableID(durableID);
        strDurableXferReq.setN2PurgeFlag(false);
        strDurableXferReq.setMandatoryFlag(false);
        log.info("Get Zone Type for Cassette");
        /*--------------------------------*/
        /*   Get Zone Type for Cassette   */
        /*--------------------------------*/
        //【step1】 - cassetteZoneTypeGet
        if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE,durableCategory)){
            Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOutRetCode = cassetteMethod.cassetteZoneTypeGet(objCommonIn, durableID);
            strDurableXferReq.setZoneType(cassetteZoneTypeGetOutRetCode.getZoneType());
            strDurableXferReq.setPriority(cassetteZoneTypeGetOutRetCode.getPriority());
        }else {
            // TODO: 2020/9/16 Durable/ReticlePod
            strDurableXferReq.setZoneType(null);
            strDurableXferReq.setPriority(null);
        }

        log.info("Abstract Stocker List");
        /*---------------------------*/
        /*   Abstract Stocker List   */
        /*---------------------------*/
        int mLen = CimArrayUtils.getSize(strWhereNextStockerInqResult.getWhereNextEqpStatus());
        List<ObjectIdentifier> machines = new ArrayList<>();
        int stkLen = 0;
        for (int j = 0; j < mLen; j++) {
            int nLen = CimArrayUtils.getSize(strWhereNextStockerInqResult.getWhereNextEqpStatus().get(j).getEqpStockerStatus());
            for (int k = 0; k < nLen; k++) {
                if (ObjectIdentifier.isNotEmpty(strWhereNextStockerInqResult.getWhereNextEqpStatus().get(j).getEqpStockerStatus().get(k).getStockerID())) {
                    log.info("strWhereNextStockerInqResult.strWhereNextEqpStatus[j].strEqpStockerStatus[k].stockerID is null");
                    if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_AUTO, strWhereNextStockerInqResult.getWhereNextEqpStatus().get(j).getEqpStockerStatus().get(k).getStockerType())) {
                        log.info("Not AutoStocker ...<continue>");
                        continue;
                    }
                    stkLen++;
                    machines.add(strWhereNextStockerInqResult.getWhereNextEqpStatus().get(j).getEqpStockerStatus().get(k).getStockerID());
                }
            }
        }
        boolean bStayOnPort = true;
        String envStayOnPort = StandardProperties.OM_XFER_STAY_ON_PORT_WITH_NO_DESTINATION.getValue();
        if (CimStringUtils.equals(envStayOnPort,"0")){
            bStayOnPort = false;
            log.info("bStayOnPort = FALSE");
        }
        log.info("If Stocker List is Nothing, Get Stocker info for Equipment");
        /*----------------------------------------------------------------*/
        /*   If Stocker List is Nothing, Get Stocker info for Equipment   */
        /*----------------------------------------------------------------*/
        if (stkLen == 0 && CimBooleanUtils.isFalse(bStayOnPort)){
            log.info("stkLen == 0 && bStayOnPort != TRUE");
            /*-------------------------------*/
            /*   Get Stocker for Equipment   */
            /*-------------------------------*/
            //【step2】 - equipmentStockerInfoGetDR
            Infos.EqpStockerInfo eqpStockerInfo = equipmentMethod.equipmentStockerInfoGetDR(objCommonIn, equipmentID);
            int tmpStkLen = CimArrayUtils.getSize(eqpStockerInfo.getEqpStockerStatusList());
            for (int j = 0; j < tmpStkLen; j++) {
                if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_AUTO,eqpStockerInfo.getEqpStockerStatusList().get(j).getStockerType())){
                    log.info("Not AutoStocker ...<continue>");
                    continue;
                }
                stkLen++;
                machines.add(eqpStockerInfo.getEqpStockerStatusList().get(j).getStockerID());
            }
            if (stkLen == 0){
                throw new ServiceException(new OmCode(retCodeConfigEx.getNoStockerForCurrentEqp(), ObjectIdentifier.fetchValue(unLoadPortID), ObjectIdentifier.fetchValue(equipmentID)), out);
            }
        }
        Validations.check((stkLen == 0) && CimBooleanUtils.isTrue(bStayOnPort), new OmCode(retCodeConfigEx.getNoXferNeeded(), ObjectIdentifier.fetchValue(unLoadPortID), ObjectIdentifier.fetchValue(equipmentID)));
        log.info("Cut Duplicate Stocker");
        /*----------------------------*/
        /*   Cut Duplicate Stocker   */
        /*----------------------------*/
        List<ObjectIdentifier> toMachines = new ArrayList<>();
        toMachines = machines;
        List<Infos.ToMachine> strToMachine = new ArrayList<>();
        strDurableXferReq.setStrToMachine(strToMachine);
        int cnt = 0;
        for (int j = 0; j < stkLen; j++) {
            boolean existFlag= false;
            for (int k = 0; k < j; k++) {
                if (ObjectIdentifier.equalsWithValue(machines.get(j), toMachines.get(k))){
                    existFlag = true;
                }
            }
            if (CimBooleanUtils.isFalse(existFlag) || j ==0){
                log.info("existFlag == FALSE || j == 0");
                Infos.ToMachine toMachine = new Infos.ToMachine();
                strToMachine.add(cnt,toMachine);
                toMachine.setToMachineID(machines.get(j));
                cnt++;
            }
        }
        /*--------------------------*/
        /*   Set Output Parameter   */
        /*--------------------------*/
        return out;
    }

    @Override
    public Results.DurableWhereNextStockerInqResult durableDestinationInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier durableID)  {
        Results.DurableWhereNextStockerInqResult result = new Results.DurableWhereNextStockerInqResult();

        //check durable category
        Boolean carrierCategoryFlag = false;
        Boolean reticelCategoryFlag = false;
        Boolean reticelPodCategoryFlag = false;
        String durableCategory = null;
        Validations.check(ObjectIdentifier.isEmpty(durableID),retCodeConfig.getInvalidParameter());
        if (null != baseCoreFactory.getBO(CimCassette.class, durableID)){
            carrierCategoryFlag = true;
            durableCategory = BizConstant.SP_DURABLECAT_CASSETTE;
        }else if (null != baseCoreFactory.getBO(CimProcessDurable.class,durableID)){
           reticelCategoryFlag = true;
           durableCategory = BizConstant.SP_DURABLECAT_RETICLE;
        }else if (null != baseCoreFactory.getBO(CimReticlePod.class,durableID)){
            reticelPodCategoryFlag = true;
            durableCategory = BizConstant.SP_DURABLECAT_RETICLEPOD;
        }
        log.info("durableCategory: {}",durableCategory);

        List<Infos.WhereNextEqpStatus> priorEqpList = new ArrayList<>();
        if (CimBooleanUtils.isTrue(carrierCategoryFlag)){
            //get carrier detail info
            //【step1】 cassetteDBInfoGetDR;
            log.info("step1 - cassetteDBInfoGetDR");
            Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfo = new Infos.CassetteDBINfoGetDRInfo();
            cassetteDBINfoGetDRInfo.setCassetteID(durableID);
            cassetteDBINfoGetDRInfo.setDurableOperationInfoFlag(true);
            cassetteDBINfoGetDRInfo.setDurableWipOperationInfoFlag(true);
            Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDRRetCode = cassetteMethod.cassetteDBInfoGetDR(objCommon, cassetteDBINfoGetDRInfo);

            Inputs.ObjSorterJobListGetDRIn cassetteInPostProcessFlagGetIn = new Inputs.ObjSorterJobListGetDRIn();
            cassetteInPostProcessFlagGetIn.setCarrierID(durableID);

            Results.CarrierDetailInfoInqResult carrierDeatilInfo = cassetteDBInfoGetDRRetCode.getCarrierDetailInfoInqResult();

            //get EquipmentIDs by DurableStatus
            //【step2】durableEquipmentOrderGetByDurableStatus
            log.info("step2 - durableEquipmentOrderGetByDurableStatus");
            Outputs.ObjLotEquipmentOrderGetByLotStatusOut durableEquipmentOrderGetByDurableStatusResult = this.durableEquipmentOrderGetByDurableStatus(objCommon,carrierDeatilInfo,durableCategory);
            priorEqpList = durableEquipmentOrderGetByDurableStatusResult.getWhereNextEqpStatusList();

            //get EquipmentStocker by DurableStatus
            //【step3】durableEquipmentStockerOrderGetByDurableStatus
            log.info("step3 - durableEquipmentStockerOrderGetByDurableStatus");
            List<Infos.WhereNextEqpStatus>  objEquipmentStockerOrderGetByLotStatusOutRetCode = this.durableEquipmentStockerOrderGetByDurableStatus(objCommon, carrierDeatilInfo, priorEqpList);

            /* *********************************/
            /*  Set Resutl information   */
            /* *********************************/
            result.setWhereNextEqpStatus(objEquipmentStockerOrderGetByLotStatusOutRetCode);
            /* **************************/
            /*  Set return structure   */
            /* **************************/
            Infos.HashedInfo durable_process_state = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Process State", hashedInfo.getHashKey())).findFirst().orElse(null);
            String durableProcessState = null;
            if (durable_process_state != null) {
                durableProcessState = durable_process_state.getHashData();
            }
            Infos.HashedInfo durable_hold_state = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Hold State", hashedInfo.getHashKey())).findFirst().orElse(null);
            String durableHoldState = null;
            if (durable_hold_state != null) {
                durableHoldState = durable_hold_state.getHashData();
            }
            Infos.HashedInfo durable_inventory = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Inventory", hashedInfo.getHashKey())).findFirst().orElse(null);
            String durableInventoryState = null;
            if (durable_inventory != null) {
                durableInventoryState = durable_inventory.getHashData();
            }
            Infos.HashedInfo durable_finished_state = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Finished State", hashedInfo.getHashKey())).findFirst().orElse(null);
            String durableFinishedState = null;
            if (durable_finished_state != null) {
                durableFinishedState = durable_finished_state.getHashData();
            }
            Infos.HashedInfo durable_production_state = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Production State", hashedInfo.getHashKey())).findFirst().orElse(null);
            String durableProductionState = null;
            if (durable_production_state != null) {
                durableProductionState = durable_production_state.getHashData();
            }

            result.setDurableID(durableID);
            result.setProductionState(durableProductionState);
            result.setHoldState(durableHoldState);
            result.setFinishedState(durableFinishedState);
            result.setProcessState(durableProcessState);
            result.setInventoryState(durableInventoryState);
            result.setDurableCategory(durableCategory);

            /* *******************************/
            /*  Set cassette information    */
            /* *******************************/
            result.setTransferStatus(carrierDeatilInfo.getCassetteStatusInfo().getTransferStatus());
            if(CimStringUtils.equals(result.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                    || CimStringUtils.equals(result.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)){
                result.setCurrentEquipmentID(carrierDeatilInfo.getCassetteStatusInfo().getEquipmentID());
            } else {
                result.setCurrentStockerID(carrierDeatilInfo.getCassetteStatusInfo().getStockerID());
            }
            result.setTransferReserveUserID(carrierDeatilInfo.getCassetteStatusInfo().getTransferReserveUserID());
            return result;
        }
        // TODO: 2020/9/16 Durable/ReticlePod
        return new Results.DurableWhereNextStockerInqResult();
    }

    @Override
    public Outputs.ObjLotEquipmentOrderGetByLotStatusOut durableEquipmentOrderGetByDurableStatus(Infos.ObjCommon objCommon, Results.CarrierDetailInfoInqResult carrierDeatilInfo,String durableCategory) {
        Outputs.ObjLotEquipmentOrderGetByLotStatusOut objLotEquipmentOrderGetByLotStatusOut = new Outputs.ObjLotEquipmentOrderGetByLotStatusOut();

        /*****************************/
        /*  Check input parameter.   */
        /*****************************/
        Validations.check(ObjectIdentifier.isEmpty(carrierDeatilInfo.getCassetteID()),retCodeConfig.getInvalidInputParam() );

        List<ObjectIdentifier> eqpIDs = new ArrayList<>();
        Integer setMachineType = -1;
        ObjectIdentifier operationID = null;
        Boolean previousEquipmentFlag = false;


        //get durable detail state
        Infos.HashedInfo durable_process_state = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Process State", hashedInfo.getHashKey())).findFirst().orElse(null);
        String durableProcessState = null;
        if (durable_process_state != null) {
            durableProcessState = durable_process_state.getHashData();
        }
        Infos.HashedInfo durable_hold_state = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Hold State", hashedInfo.getHashKey())).findFirst().orElse(null);
        String durableHoldState = null;
        if (durable_hold_state != null) {
            durableHoldState = durable_hold_state.getHashData();
        }
        Infos.HashedInfo durable_inventory = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Inventory", hashedInfo.getHashKey())).findFirst().orElse(null);
        String durableInventory = null;
        if (durable_inventory != null) {
            durableInventory = durable_inventory.getHashData();
        }

        /*****************************/
        /* When durable is on Floor.     */
        /*****************************/
        if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE,durableCategory)){
            ObjectIdentifier cassetteID = carrierDeatilInfo.getCassetteID();
            switch (CimStringUtils.equals(SP_DURABLE_INVENTORYSTATE_ONFLOOR,durableInventory) ? 1 : 0) {
                case 1: {
                    /****************************************/
                    /*                                      */
                    /*  Get default queued machine List     */
                    /*                                      */
                    /****************************************/
                    List<Infos.LotEquipmentList> durableQueuedMachineList = carrierDeatilInfo.getStrDurableOperationInfo().getStrEquipmentList();
                    int nQueuedEqpLen = CimArrayUtils.getSize(durableQueuedMachineList);
                    if (nQueuedEqpLen > 0) {
                        eqpIDs = durableQueuedMachineList.stream().map(lotEquipmentList -> lotEquipmentList.getEquipmentID()).collect(Collectors.toList());
                        eqpIDs = this.durableQueuedMachinesGetByOperationOrder(objCommon,eqpIDs,carrierDeatilInfo.getStrDurableOperationInfo().getOperationID());
                        setMachineType = MethodEnums.LotEquipmentOrderGetByLotStatusEnums.setQueuedMachine.getType();
                        break;
                    }
                    /*************************************/
                    /* When held Cassette is detected    */
                    /*************************************/
                    Boolean previousHoldFlag = false;
                    if (CimStringUtils.equals(durableHoldState, SP_DURABLE_HOLDSTATE_ONHOLD)) {
                        /********************************/
                        /*    Check hold detail info    */
                        /********************************/
                        //【step1】: durableHoldRecordGetDR
                        log.info("step1 - durableHoldRecordGetDR");
                        List<Infos.DurableHoldRecord> durableHoldRecords = this.durableHoldRecordGetDR(objCommon,cassetteID, durableCategory);

                        int holdRecLen = CimArrayUtils.getSize(durableHoldRecords);
                        for (int i = 0; i < holdRecLen; i++) {
                            if (CimStringUtils.equals(durableHoldRecords.get(i).getResponsibleOperationMark(), BizConstant.SP_RESPONSIBLEOPERATION_PREVIOUS)) {
                                previousHoldFlag = true;
                                break;
                            }
                        }
                    }
                    /*************************************/
                    /* When cassette is on Floor and     */
                    /*  - Previous Hold cassette         */
                    /*************************************/
                    if (CimStringUtils.equals(durableHoldState, SP_DURABLE_HOLDSTATE_NOTONHOLD) || CimBooleanUtils.isTrue(previousHoldFlag)) {
                        String transferStatus = carrierDeatilInfo.getCassetteStatusInfo().getTransferStatus();
                        if (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONIN)
                                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_BAYIN)
                                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_MANUALIN)
                                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_SHELFIN)
                                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEIN)
                                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALIN)) {
                            setMachineType = MethodEnums.LotEquipmentOrderGetByLotStatusEnums.setNoMachine.getType();
                            break;
                        } else {
                            previousEquipmentFlag = true;
                            CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
                            Validations.check(null == cassette,retCodeConfig.getNotFoundCassette());
                            CimControlJob aControlJob = cassette.getControlJob();
                            if (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) && aControlJob == null) {
                                setMachineType = MethodEnums.LotEquipmentOrderGetByLotStatusEnums.setCassetteMachine.getType();
                                break;
                            } else {
                                /********************************/
                                /*                              */
                                /* Get the previous equipment   */
                                /*                              */
                                /********************************/
                                CimDurableProcessFlowContext durableProcessFlowContext = cassette.getDurableProcessFlowContext();
                                Validations.check(durableProcessFlowContext == null, retCodeConfig.getNotFoundPfx());

                                CimDurableProcessOperation prevPO = durableProcessFlowContext.getPreviousProcessOperation();
                                Validations.check(prevPO == null, retCodeConfig.getNotFoundPoForLot());

                                com.fa.cim.newcore.bo.pd.CimProcessDefinition aPD = prevPO.getProcessDefinition();
                                Validations.check(aPD == null, retCodeConfig.getNotFoundProcessDefinition());

                                /********************************/
                                /*                              */
                                /*   Get queued machine List    */
                                /*                              */
                                /********************************/
                                operationID = new ObjectIdentifier(aPD.getIdentifier(), aPD.getPrimaryKey());
                                setMachineType = MethodEnums.LotEquipmentOrderGetByLotStatusEnums.setOperationDispatchedMachine.getType();
                                break;
                            }
                        }
                    }
                    /*********************************/
                    /* When casstte is on Floor and  */
                    /*   Held on Current Operation.  */
                    /*********************************/
                    else {
                        operationID = carrierDeatilInfo.getStrDurableOperationInfo().getOperationID();
                        setMachineType = MethodEnums.LotEquipmentOrderGetByLotStatusEnums.setOperationDispatchedMachine.getType();
                        break;
                    }
                }
                case 0: {
                    /**********************************/
                    /* When cassette is in Bank.           */
                    /**********************************/
                    CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
                    Validations.check(null == cassette,retCodeConfig.getNotFoundCassette());
                    CimBank aBank = cassette.getBank();
                    Validations.check(aBank == null, retCodeConfig.getNotFoundBank());

                    CimStorageMachine aStocker = aBank.getStorageMachine();
                    if (aStocker == null) {
                        if (!ObjectIdentifier.isEmptyWithValue(carrierDeatilInfo.getCassetteStatusInfo().getEquipmentID())) {
                            previousEquipmentFlag = true;
                            setMachineType = MethodEnums.LotEquipmentOrderGetByLotStatusEnums.setCassetteMachine.getType();
                            break;
                        }
                    }
                    setMachineType = MethodEnums.LotEquipmentOrderGetByLotStatusEnums.setNoMachine.getType();
                    break;
                }
                default:
                    break;
            }
            switch (MethodEnums.LotEquipmentOrderGetByLotStatusEnums.getByType(setMachineType)){
                case setQueuedMachine:
                    break;
                case setOperationDispatchedMachine:{
                    /*******************************************************/
                    /*    Call processDispatchEquipmentsForDurableGetDR    */
                    /*******************************************************/
                    String eqpListFlag = StandardProperties.OM_STEP_EQP_LIST_INQ.getValue();
                    if (!CimStringUtils.equals(eqpListFlag, "0")) {
                        if (!CimStringUtils.equals(operationID,"*")) {
                            //【step2】: processDispatchEquipmentsForDurableGetDR
                            log.info("step2 - processDispatchEquipmentsForDurableGetDR");
                            List<ObjectIdentifier> processDispatchEquipmentsResult = processMethod.processDispatchEquipmentsForDurableGetDR(objCommon, operationID);
                            eqpIDs = processDispatchEquipmentsResult;
                            break;
                        }
                    }
                    break;
                }
                case setCassetteMachine:{
                    /*************************************/
                    /*  Set Cassette related Machine.    */
                    /*************************************/
                    eqpIDs.add(carrierDeatilInfo.getCassetteStatusInfo().getEquipmentID());
                    break;
                }
                case setNoMachine:{
                    break;
                }
                default:
                    break;
            }
            if(!CimObjectUtils.isEmpty(eqpIDs)){
                Boolean checkInhibitFlag = true;
                Boolean checkMachineAvailabilityFlag = true;
                if(CimBooleanUtils.isTrue(previousEquipmentFlag)){
                    /********************************************************************************/
                    /*  Responsible operation of this Cassette is previous operation.               */
                    /*  So it should not be checked its availability for the previous equipment.    */
                    /********************************************************************************/
                    checkInhibitFlag = false;
                    checkMachineAvailabilityFlag = false;
                }
                /**********************************/
                /*  Sort the Equipment.           */
                /**********************************/
                //【step3】: equipmentPriorityOrderGetByLotAvailability
                log.info("step3 - equipmentPriorityOrderGetByLotAvailability with out LotID");
                Outputs.ObjEquipmentPriorityOrderGetByLotAvailabilityOut objEquipmentPriorityOrderGetByLotAvailabilityOutRetCode = equipmentMethod.equipmentPriorityOrderGetByLotAvailability(objCommon, eqpIDs, null, checkInhibitFlag, checkMachineAvailabilityFlag);
                /**********************************/
                /*  Set Equipment information.    */
                /**********************************/
                objLotEquipmentOrderGetByLotStatusOut.setWhereNextEqpStatusList(objEquipmentPriorityOrderGetByLotAvailabilityOutRetCode.getWhereNextEqpStatuseList());
                objLotEquipmentOrderGetByLotStatusOut.setAvailableEqpExistFlag(objEquipmentPriorityOrderGetByLotAvailabilityOutRetCode.getAvailableEqpExistFlag());
            }  else {
                objLotEquipmentOrderGetByLotStatusOut.setAvailableEqpExistFlag(true);
            }
        }

        return objLotEquipmentOrderGetByLotStatusOut;


    }

    @Override
    public List<Infos.WhereNextEqpStatus> durableEquipmentStockerOrderGetByDurableStatus(Infos.ObjCommon objCommon, Results.CarrierDetailInfoInqResult carrierDeatilInfo, List<Infos.WhereNextEqpStatus> eqpStatusList) {
        List<Infos.WhereNextEqpStatus> whereNextEqpStatusList = new ArrayList<>();
        /*****************************************/
        /*  Check cassette availability for UTS  */
        /*****************************************/
        Boolean notCandidateForUTSFlag = false;
        int eqpLen = CimArrayUtils.getSize(eqpStatusList);
        Infos.HashedInfo durable_inventory = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Inventory", hashedInfo.getHashKey())).findFirst().orElse(null);
        String durableInventory = null;
        if (durable_inventory != null) {
            durableInventory = durable_inventory.getHashData();
        }
        Infos.HashedInfo durable_hold_state = carrierDeatilInfo.getCassetteStatusInfo().getStrDurableStatusList().stream().filter(hashedInfo -> CimStringUtils.equals("Durable Hold State", hashedInfo.getHashKey())).findFirst().orElse(null);
        String durableHoldState = null;
        if (durable_hold_state != null) {
            durableHoldState = durable_hold_state.getHashData();
        }
        if(eqpLen != 0){
            /*******************************/
            /* Check the carrier status    */
            /*******************************/
            log.info("Step1 - cassetteGetStatusDR");
            Outputs.ObjCassetteStatusOut cassetteStatusDR = cassetteMethod.cassetteGetStatusDR(objCommon, carrierDeatilInfo.getCassetteID());
            if(!CimStringUtils.equals(cassetteStatusDR.getDurableState(), BizConstant.SP_DRBL_STATE_AVAILABLE)){
                notCandidateForUTSFlag = true;
            }
            /***************************/
            /* Check the Lot status    */
            /***************************/
            if(!CimStringUtils.equals(SP_DURABLE_INVENTORYSTATE_ONFLOOR,durableInventory) || CimStringUtils.equals(SP_DURABLE_HOLDSTATE_ONHOLD,durableHoldState)){
                notCandidateForUTSFlag = true;
            }
        }
        Boolean setCurrentStockerFlag = false;
        List<Infos.EqpStockerStatus> stockerList = new ArrayList<>();
        List<Infos.EqpStockerStatus> UTSstockerList = new ArrayList<>();
        if (eqpLen > 0){
            for (Infos.WhereNextEqpStatus eqpStatus : eqpStatusList){
                stockerList = new ArrayList<>();
                UTSstockerList = new ArrayList<>();
                /***************************/
                /* Check inhibit status    */
                /***************************/
                Boolean notCandidateForUTSByInhibitFlag = false;
                if(!CimObjectUtils.isEmpty(eqpStatus.getEntityInhibitions()) || CimBooleanUtils.isFalse(eqpStatus.isEquipmentAvailableFlag())){
                    notCandidateForUTSByInhibitFlag = true;
                }
                /***********************************/
                /* Get all stocker of equipment    */
                /***********************************/
                log.info("Step2 - equipmentAllStockerGetByUTSPriorityDR(Get all stocker of equipment)");
                Outputs.ObjEquipmentAllStockerGetByUTSPriorityDROut objEquipmentAllStockerGetByUTSPriorityDROutRetCode = equipmentMethod.equipmentAllStockerGetByUTSPriorityDR(objCommon, eqpStatus.getEquipmentID());

                /**************************************************************/
                /*  Keep UTS and stocker list for force destination decision  */
                /**************************************************************/
                List<Infos.EqpStockerStatus> UTSstockers = objEquipmentAllStockerGetByUTSPriorityDROutRetCode.getUTSstockers();
                if(CimArrayUtils.getSize(UTSstockers) > 0 && !notCandidateForUTSFlag && !notCandidateForUTSByInhibitFlag){
                    /**************************************************/
                    /*  Sort UTS sequence by Lot status  None LotID   */
                    /**************************************************/
                    log.info("Step3 - stockerUTSPriorityOrderGetByLotAvailability(Sort UTS sequence by Lot status)..none lotID for durable");
                    List<Infos.EqpStockerStatus> eqpStockerStatuses = stockerMethod.stockerUTSPriorityOrderGetByLotAvailability(objCommon, UTSstockers, null);
                    UTSstockerList.addAll(eqpStockerStatuses);
                }
                List<Infos.EqpStockerStatus> stockers = objEquipmentAllStockerGetByUTSPriorityDROutRetCode.getStockers();
                if(!CimObjectUtils.isEmpty(stockers)){
                    /*****************************************************/
                    /*  Sort stocker sequence by Lot status None LotID   */
                    /*****************************************************/
                    log.info("Step4 - stockerPriorityOrderGetByLotAvailability(Sort stocker sequence by Lot status)..none lotID for durable");
                    List<Infos.EqpStockerStatus> eqpStockerStatuses = stockerMethod.stockerPriorityOrderGetByLotAvailability(objCommon, stockers, null);
                    stockerList.addAll(eqpStockerStatuses);
                }
                /*************************/
                /* Set return structure  */
                /*************************/
                int utsLen = CimArrayUtils.getSize(UTSstockerList);
                int stkLen = CimArrayUtils.getSize(stockerList);
                if(utsLen + stkLen == 0){
                    /****************************************************************************************/
                    /*  Even if NextUTS is full or not available, set it by force when the cassette is EI   */
                    /****************************************************************************************/
                    whereNextEqpStatusList.add(eqpStatus);
                    if (CimStringUtils.equals(carrierDeatilInfo.getCassetteStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                        if(!CimObjectUtils.isEmpty(UTSstockers) && !notCandidateForUTSFlag && !notCandidateForUTSByInhibitFlag){
                            eqpStatus.setEqpStockerStatus(UTSstockers);
                        }
                        /*****************************************************************************/
                        /*  If cassette is EI, then try to get Stocker from cassette location info   */
                        /*****************************************************************************/
                        else {
                            log.info("Step5 - equipmentStockerInfoGetDR");
                            Infos.EqpStockerInfo eqpStockerInfo = equipmentMethod.equipmentStockerInfoGetDR(objCommon, carrierDeatilInfo.getCassetteStatusInfo().getEquipmentID());
                            List<Infos.EqpStockerStatus> eqpStockerStatusList = eqpStockerInfo.getEqpStockerStatusList();
                            if(!CimObjectUtils.isEmpty(eqpStockerStatusList)){
                                eqpStatus.setEqpStockerStatus(eqpStockerStatusList);
                            }
                        }
                    }
                    /******************************************/
                    /*  Equipment does not have any stocker.  */
                    /******************************************/
                    else {
                        eqpStatus.setEqpStockerStatus(new ArrayList<>());
                        setCurrentStockerFlag = true;
                    }
                } else {
                    /*********************************/
                    /*  Equipment has some stocker.  */
                    /*********************************/
                    whereNextEqpStatusList.add(eqpStatus);
                    List<Infos.EqpStockerStatus> eqpStockerStatusList = new ArrayList<>();
                    eqpStatus.setEqpStockerStatus(eqpStockerStatusList);
                    if (utsLen > 0){
                        eqpStockerStatusList.addAll(UTSstockerList);
                    }
                    for (int stkCnt = 0; stkCnt < stkLen; stkCnt++){
                        Infos.EqpStockerStatus eqpStockerStatus = stockerList.get(stkCnt);
                        /********************************/
                        /*  Reassign stocker priority   */
                        /********************************/
                        if(utsLen != 0){
                            eqpStockerStatus.setStockerPriority(String.valueOf(stkCnt));
                        }
                        eqpStockerStatusList.add(eqpStockerStatus);
                    }
                }
            }
        }
        if(eqpLen == 0){
            /*****************************/
            /* When Lot is on Floor.     */
            /*****************************/
            if(CimStringUtils.equals(SP_DURABLE_INVENTORYSTATE_ONFLOOR,durableInventory)){
                if(!ObjectIdentifier.isEmptyWithValue(carrierDeatilInfo.getCassetteStatusInfo().getStockerID())){
                    log.info("The cassette is not in Equipment. Set current stocker for cassette.");
                    setCurrentStockerFlag = true;
                    whereNextEqpStatusList.add(new Infos.WhereNextEqpStatus());
                    whereNextEqpStatusList.get(0).setEqpStockerStatus(new ArrayList<>());
                } else {
                    log.info("The cassette is not in Equipment, and cannot find any stockers.");
                    //Already set stockers of equipment if the Lot is on Equipment.
                }
            }
            /**********************************/
            /* When Lot is in Bank.           */
            /*   => Stay in the current Bank. */
            /**********************************/
            else {
                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, carrierDeatilInfo.getCassetteID());
                Validations.check(null == cassette,retCodeConfig.getNotFoundCassette());
                CimBank aBank = cassette.getBank();
                Validations.check(aBank == null, retCodeConfig.getNotFoundBank());
                CimStorageMachine aStocker = aBank.getStorageMachine();
                if(aStocker != null){
                    Infos.WhereNextEqpStatus whereNextEqpStatus = new Infos.WhereNextEqpStatus();
                    whereNextEqpStatusList.add(whereNextEqpStatus);
                    List<Infos.EqpStockerStatus> eqpStockerStatusList = new ArrayList<>();
                    whereNextEqpStatus.setEqpStockerStatus(eqpStockerStatusList);
                    Infos.EqpStockerStatus eqpStockerStatus = new Infos.EqpStockerStatus();
                    eqpStockerStatusList.add(eqpStockerStatus);
                    ObjectIdentifier stockerID = new ObjectIdentifier(aStocker.getIdentifier(),aStocker.getPrimaryKey());
                    eqpStockerStatus.setStockerID(stockerID);
                    log.info("Step6 - stockerBaseInfoGet");
                    Results.StockerInfoInqResult stockerInfoInq100ResultRetCode = stockerMethod.stockerBaseInfoGet(objCommon, stockerID);
                    eqpStockerStatus.setStockerType(stockerInfoInq100ResultRetCode.getStockerType());
                    eqpStockerStatus.setStockerStatus(stockerInfoInq100ResultRetCode.getActualStatusCode());
                    eqpStockerStatus.setOhbFlag(stockerInfoInq100ResultRetCode.getUtsFlag());
                    eqpStockerStatus.setMaxOHBFlag(stockerInfoInq100ResultRetCode.getMaxUTSCapacity());
                    eqpStockerStatus.setStockerPriority("0");
                } else {
                    /**********************************************************************************************/
                    /*   Set the current Stocker if the Lot is in Bank(The bank has no stocker) and in Stocker.   */
                    /**********************************************************************************************/
                    if(!ObjectIdentifier.isEmptyWithValue(carrierDeatilInfo.getCassetteStatusInfo().getStockerID())){
                        setCurrentStockerFlag = true;
                        whereNextEqpStatusList.add(new Infos.WhereNextEqpStatus());
                        whereNextEqpStatusList.get(0).setEqpStockerStatus(new ArrayList<>());
                    } else {
                        //Already set stockers of equipment if the Lot is on Equipment.
                    }
                }
            }
        }
        /********************************************************************************************/
        /*  At least one equipment doesn't have stocker. Get it from cassettte related equipment.   */
        /********************************************************************************************/
        if(CimBooleanUtils.isTrue(setCurrentStockerFlag)){
            /********************************************/
            /* Get Current Machine detail information   */
            /********************************************/
            Infos.EqpStockerStatus aCurrentStockerInfo = new Infos.EqpStockerStatus();
            if(!ObjectIdentifier.isEmptyWithValue(carrierDeatilInfo.getCassetteStatusInfo().getStockerID())){
                log.info("Step7 - stockerBaseInfoGet");
                Results.StockerInfoInqResult stockerInfoInq100ResultRetCode = stockerMethod.stockerBaseInfoGet(objCommon, carrierDeatilInfo.getCassetteStatusInfo().getStockerID());
                aCurrentStockerInfo.setStockerID(carrierDeatilInfo.getCassetteStatusInfo().getStockerID());
                aCurrentStockerInfo.setStockerType(stockerInfoInq100ResultRetCode.getStockerType());
                aCurrentStockerInfo.setOhbFlag(stockerInfoInq100ResultRetCode.getUtsFlag());
                aCurrentStockerInfo.setMaxOHBFlag(stockerInfoInq100ResultRetCode.getMaxUTSCapacity());
                aCurrentStockerInfo.setStockerPriority("0");
            }
            /********************************************/
            /*  If stocker type is "Auto", then set it. */
            /*  Else, no stocker is set.                */
            /********************************************/
            if(CimStringUtils.equals(aCurrentStockerInfo.getStockerType(), BizConstant.SP_STOCKER_TYPE_AUTO)){
                int eLen = CimArrayUtils.getSize(whereNextEqpStatusList);
                for(int i = 0; i < eLen; i++){
                    int sLen = CimArrayUtils.getSize(whereNextEqpStatusList.get(i).getEqpStockerStatus());
                    if(sLen == 0){
                        whereNextEqpStatusList.get(i).getEqpStockerStatus().add(aCurrentStockerInfo);
                    }
                }
            }
        }
        return whereNextEqpStatusList;
    }

    @Override
    public List<ObjectIdentifier> durableQueuedMachinesGetByOperationOrder(Infos.ObjCommon objCommon, List<ObjectIdentifier> eqpIDs, ObjectIdentifier operationID) {
        List<ObjectIdentifier> result = null;
        if (!CimObjectUtils.isEmpty(eqpIDs)) {

            /***********************************************/
            /*    Call process_dispatchEquipments_GetDR    */
            /***********************************************/
            log.info("Step1 - processDispatchEquipmentsForDurableGetDR");
            List<ObjectIdentifier> processDispatchEquipmentResult = processMethod.processDispatchEquipmentsForDurableGetDR(objCommon, operationID);

            List<ObjectIdentifier> processEqpIDs = processDispatchEquipmentResult;
            /***************************************************/
            /* Sort by eqp_id sequence which are defined in PD */
            /***************************************************/
            log.info("Step2 - Sort by eqp_id sequence which are defined in PD");
            int lotEqpLen = CimArrayUtils.getSize(eqpIDs);
            int procEqpLen = CimArrayUtils.getSize(processEqpIDs);
            int count = 0;
            result = new ArrayList<>();
            for (int i = 0; i < procEqpLen; i++) {
                for (int j = 0; j < lotEqpLen; j++) {
                    if (ObjectIdentifier.equalsWithValue(processEqpIDs.get(i), eqpIDs.get(j))) {
                        if (count < lotEqpLen) {
                            result.add(eqpIDs.get(j));
                            count++;
                        }
                        break;
                    }
                }
            }
            if (count < lotEqpLen) {
                for (int i = 0; i < lotEqpLen; i++) {
                    Boolean existFlag = false;
                    for (int j = 0; j < count; j++) {
                        if (ObjectIdentifier.equalsWithValue(eqpIDs.get(i), result.get(j))) {
                            existFlag = true;
                            break;
                        }
                    }
                    if (!existFlag) {
                        if (count < lotEqpLen) {
                            result.add(eqpIDs.get(i));
                            count++;
                        }
                    }
                }
            }
        }
        return result;
    }

    public String durableOnRouteStateGet (
            Infos.ObjCommon                   strObjCommonIn,
            Infos.DurableOnRouteStateGetIn   strDurableOnRouteStateGetin) {
        log.info("PPTManager_i::durable_OnRouteState_Get");

        String durableOnRouteState=null;
        if ( CimStringUtils.equals(strDurableOnRouteStateGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_CASSETTE) ) {
            CimCassette aCassette;
            aCassette =baseCoreFactory.getBO(CimCassette.class,
                    strDurableOnRouteStateGetin.getDurableID());
            durableOnRouteState = aCassette.getDurableOnRouteState();
        } else if ( CimStringUtils.equals(strDurableOnRouteStateGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLEPOD) ) {
            CimReticlePod aReticlePod;
            aReticlePod = baseCoreFactory.getBO(CimReticlePod.class,
                    strDurableOnRouteStateGetin.getDurableID());
            durableOnRouteState = aReticlePod.getDurableOnRouteState();
        } else if ( CimStringUtils.equals(strDurableOnRouteStateGetin.getDurableCategory(), BizConstant.SP_DURABLECAT_RETICLE) ) {
            CimProcessDurable aReticle;
            aReticle = baseCoreFactory.getBO(CimProcessDurable.class,
                    strDurableOnRouteStateGetin.getDurableID());
            durableOnRouteState = aReticle.getDurableOnRouteState();
        } else {
            log.info("{}", "durableCategory is invalid!");
            Validations.check(true, retCodeConfig.getInvalidDurableCategory(), strDurableOnRouteStateGetin.getDurableCategory());
        }

        log.info("{} {}", "durableOnRouteStatus is", durableOnRouteState);
        //--------------------------
        // Return to Caller
        //--------------------------
        return durableOnRouteState;
    }

    @Override
    public List<Infos.PodInErack> podInErack(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        List<Infos.PodInErack> podInEracks = new ArrayList<>();

        Results.StockerInfoInqResult stockerInfoInqResult = cassetteMethod.cassetteFillInTxLGQ004DR(objCommon, stockerID);
        List<Infos.CarrierInStocker> strCarrierInStockers = stockerInfoInqResult.getStrCarrierInStocker();
        if (!CimArrayUtils.isEmpty(strCarrierInStockers)) {
            for (Infos.CarrierInStocker strCarrierInStocker : strCarrierInStockers) {
                String transferJobStatus = strCarrierInStocker.getTransferJobStatus();
                if (!CimArrayUtils.binarySearch(new String[]{ //验证cast是否为stockerin状态，如果不是则略过，reticle没有检查是因为stockerFillInTxPDQ006DR方法中已经检查。
                        BizConstant.SP_TRANSSTATE_SHELFIN,
                        BizConstant.SP_TRANSSTATE_MANUALIN,
                        BizConstant.SP_TRANSSTATE_STATIONIN,
                        BizConstant.SP_TRANSSTATE_BAYIN,
                        BizConstant.SP_TRANSSTATE_ABNORMALIN
                }, transferJobStatus)) {
                    continue;
                }
                CimCassetteDO cimCassetteExample = new CimCassetteDO();
                cimCassetteExample.setCassetteID(ObjectIdentifier.fetchValue(strCarrierInStocker.getCassetteID()));

                cimJpaRepository.findOne(Example.of(cimCassetteExample)).ifPresent(cassette -> {
                    Infos.PodInErack podInErack = new Infos.PodInErack();
                    podInErack.setPodID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
                    podInErack.setShelfPositionX(cassette.getShelfPositionX());
                    podInErack.setShelfPositionY(cassette.getShelfPositionY());
                    podInErack.setShelfPositionZ(cassette.getShelfPositionZ());
                    podInErack.setShelfType("FOUP");
                    podInEracks.add(podInErack);
                });
            }
        }

        Results.ReticleStocInfoInqResult reticleStocInfoInqResult = stockerMethod.stockerFillInTxPDQ006DR(objCommon, stockerID);
        List<Infos.ReticlePodInStocker> reticlePodInStocker = reticleStocInfoInqResult.getReticlePodInStocker();
        if (!CimObjectUtils.isEmpty(reticlePodInStocker)) {
            for (Infos.ReticlePodInStocker podInStocker : reticlePodInStocker) {
                CimReticlePodDO cimReticlePodExample = new CimReticlePodDO();
                cimReticlePodExample.setReticlePodID(ObjectIdentifier.fetchValue(podInStocker.getReticlePodID()));
                cimJpaRepository.findOne(Example.of(cimReticlePodExample)).ifPresent(reticlePod -> {
                    Infos.PodInErack podInErack = new Infos.PodInErack();
                    podInErack.setPodID(new ObjectIdentifier(reticlePod.getIdentifier(), reticlePod.getId()));
                    podInErack.setShelfPositionX(reticlePod.getShelfPositionX());
                    podInErack.setShelfPositionY(reticlePod.getShelfPositionY());
                    podInErack.setShelfPositionZ(reticlePod.getShelfPositionZ());
                    podInErack.setShelfType("ReticlePOD");
                    podInEracks.add(podInErack);
                });
            }
        }
        return podInEracks;
    }

    @Override
    public void postProcessQueueForceDeleteDR(Infos.ObjCommon objCommon, Params.StrLotCassettePostProcessForceDeleteReqInParams strLotCassettePostProcessForceDeleteReqInParams) {
        log.info("PPTManager_i::postProcessQueue_ForceDeleteDR");
        log.info("cassetteID : {}", ObjectIdentifier.fetchValue(strLotCassettePostProcessForceDeleteReqInParams.getCassetteID()));
        log.info("lotID : {}", ObjectIdentifier.fetchValue(strLotCassettePostProcessForceDeleteReqInParams.getLotID()));

        int cassetteIdLength = CimStringUtils.length(ObjectIdentifier.fetchValue(strLotCassettePostProcessForceDeleteReqInParams.getCassetteID()));
        int lotIdLength = CimStringUtils.length(ObjectIdentifier.fetchValue(strLotCassettePostProcessForceDeleteReqInParams.getLotID()));

        //---------------------------
        //  Check input parameter
        //---------------------------
        Validations.check((0 == cassetteIdLength
                && 0 == lotIdLength)
                ||
                (0 < cassetteIdLength
                        && 0 < lotIdLength), retCodeConfig.getInvalidParameter());

        //------------------------
        //  Get LotID Sequence
        //------------------------
        List<ObjectIdentifier> lotIDs = Lists.newArrayList();
        CimCassette cimCassette;

        //DSN000096126 Add Start
        String durableCategory = "";
        CimReticlePod cimReticlePod;
        CimProcessDurable cimProcessDurable;

        if (!ObjectIdentifier.isEmpty(strLotCassettePostProcessForceDeleteReqInParams.getLotID())) {
            log.info("Lot is specified.");
            lotIDs.add(strLotCassettePostProcessForceDeleteReqInParams.getLotID());
        } else if (!ObjectIdentifier.isEmpty(strLotCassettePostProcessForceDeleteReqInParams.getCassetteID())) {
            log.info("Cassette is specified.");

            //DSN000096126 Add Start
            durableCategory = durableDurableCategoryGet(objCommon, strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());

            List<Lot> aLotSequence = null;
            if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
                //DSN000096126 Add End
                //DSN000096126 Indentation Start
                //-------------------------
                //  Get Cassette Object
                //-------------------------
                cimCassette = baseCoreFactory.getBO(CimCassette.class, strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());

                //------------------------------
                //  Get all Lots in Cassette
                //------------------------------
                //DSN000096126                LotSequence*    aLotSequence = NULL;
                //DSN000096126                LotSequence_var aLotSequenceVar;

                aLotSequence = cimCassette.allLots();

            } else {
                aLotSequence = Lists.newArrayList();
            }
            //DSN000096126 Add End
            lotIDs = aLotSequence.parallelStream().map(lot -> ObjectIdentifier.build(lot.getIdentifier(), lot.getPrimaryKey())).collect(Collectors.toList());
        }

        Boolean bCastMultiLotTypeUpdate = false;     //DSN000050720
        int nLotLen = lotIDs.size();
        log.info("lotIDs.length{}", nLotLen);

        for (int i = 0; i < nLotLen; i++) {
            String hFQPOSTPROC_LOT_ID = ObjectIdentifier.fetchValue(lotIDs.get(i));
            log.info("hFQPOSTPROC_LOT_ID = {}", hFQPOSTPROC_LOT_ID);

            String sql = "SELECT DISTINCT LINK_KEY\n" +
                    "            FROM   OQPPRC\n" +
                    "            WHERE  LOT_ID = ?1";

            List<CimPostProcessDO> postProcessDos = cimJpaRepository.query(sql, CimPostProcessDO.class, hFQPOSTPROC_LOT_ID);
            for (CimPostProcessDO postProcessDo : postProcessDos) {
                if (!bCastMultiLotTypeUpdate) {
                    Infos.PostProcessTargetObject strPostProcessTargetObject = new Infos.PostProcessTargetObject();
                    strPostProcessTargetObject.setLotID(ObjectIdentifier.build(postProcessDo.getLotID(), postProcessDo.getLotObj()));

                    Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn = new Inputs.PostProcessQueueListDRIn();
                    postProcessQueueListDRIn.setKey(postProcessDo.getDkey());
                    postProcessQueueListDRIn.setSeqNo(-1L);
                    postProcessQueueListDRIn.setWatchDogName("");
                    postProcessQueueListDRIn.setPostProcId(BizConstant.SP_POSTPROCESS_ACTIONID_PARALLELEXECFINALIZE);
                    postProcessQueueListDRIn.setSyncFlag(-1L);
                    postProcessQueueListDRIn.setTxId("");
                    postProcessQueueListDRIn.setTargetType("");
                    postProcessQueueListDRIn.setStrPostProcessTargetObject(strPostProcessTargetObject);
                    postProcessQueueListDRIn.setStatus("");
                    postProcessQueueListDRIn.setPassedTime(-1L);
                    postProcessQueueListDRIn.setClaimUserId("");
                    postProcessQueueListDRIn.setStartCreateTimeStamp("");
                    postProcessQueueListDRIn.setEndCreateTieStamp("");
                    postProcessQueueListDRIn.setStartUpdateTimeStamp("");
                    postProcessQueueListDRIn.setEndUpdateTimeStamp("");
                    postProcessQueueListDRIn.setMaxCount(1L);
                    postProcessQueueListDRIn.setCommittedReadFlag(true);
                    Outputs.ObjPostProcessQueListDROut objPostProcessQueListDROut = postProcessMethod.postProcessQueueListDR(
                            objCommon, postProcessQueueListDRIn);
                    if (0 < objPostProcessQueListDROut.getStrActionInfoSeq().size()) {
                        log.info("strActionInfoSeq.length() : {}", objPostProcessQueListDROut.getStrActionInfoSeq().size());
                        bCastMultiLotTypeUpdate = true;
                    }
                }

                //DSN000050720 Add End
                //----------------------------------------
                //  Delete Post Process Queue by D_KEY
                //----------------------------------------
                log.info("# Delete Post Process Queue by D_KEY {}", postProcessDo.getDkey());

                //DSIV00000201                pptPostProcessActionInfoSequence strPostProcessActionInfoSeq;
                List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq = Lists.newArrayList();
                Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
                postProcessActionInfo.setDKey(postProcessDo.getDkey());
                postProcessActionInfo.setSequenceNumber(postProcessDo.getSeqNo());
                strPostProcessActionInfoSeq.add(postProcessActionInfo);
                postProcessMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, strPostProcessActionInfoSeq);
            }

            //--------------------------------------------------
            //  Set InPostProcessFlag of Lot/Cassette to OFF
            //--------------------------------------------------
            lotMethod.lotInPostProcessFlagSet(objCommon, lotIDs.get(i), true);

        } // for( CORBA::Long i = 0; i < nLotLen; i++ )

        if (0 == nLotLen) {
            log.info("Cassette is empty.");
            String sql = "SELECT DISTINCT LINK_KEY\n" +
                    "            FROM   OQPPRC\n" +
                    "            WHERE  CARRIER_ID = ?1";
            List<CimPostProcessDO> postProcessDOS = cimJpaRepository.query(sql, CimPostProcessDO.class, ObjectIdentifier.fetchValue(strLotCassettePostProcessForceDeleteReqInParams.getCassetteID()));

            for (CimPostProcessDO postProcessDo : postProcessDOS) {
                if (!bCastMultiLotTypeUpdate) {
                    //DSN000050720 Add Start
                    Infos.PostProcessTargetObject strPostProcessTargetObject = new Infos.PostProcessTargetObject();
                    strPostProcessTargetObject.setLotID(ObjectIdentifier.build(postProcessDo.getLotID(), postProcessDo.getLotObj()));

                    Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn = new Inputs.PostProcessQueueListDRIn();
                    postProcessQueueListDRIn.setKey(postProcessDo.getDkey());
                    postProcessQueueListDRIn.setSeqNo(-1L);
                    postProcessQueueListDRIn.setWatchDogName("");
                    postProcessQueueListDRIn.setPostProcId(BizConstant.SP_POSTPROCESS_ACTIONID_PARALLELEXECFINALIZE);
                    postProcessQueueListDRIn.setSyncFlag(-1L);
                    postProcessQueueListDRIn.setTxId("");
                    postProcessQueueListDRIn.setTargetType("");
                    postProcessQueueListDRIn.setStrPostProcessTargetObject(strPostProcessTargetObject);
                    postProcessQueueListDRIn.setStatus("");
                    postProcessQueueListDRIn.setPassedTime(-1L);
                    postProcessQueueListDRIn.setClaimUserId("");
                    postProcessQueueListDRIn.setStartCreateTimeStamp("");
                    postProcessQueueListDRIn.setEndCreateTieStamp("");
                    postProcessQueueListDRIn.setStartUpdateTimeStamp("");
                    postProcessQueueListDRIn.setEndUpdateTimeStamp("");
                    postProcessQueueListDRIn.setMaxCount(1L);
                    postProcessQueueListDRIn.setCommittedReadFlag(true);
                    Outputs.ObjPostProcessQueListDROut objPostProcessQueListDROut = postProcessMethod.postProcessQueueListDR(
                            objCommon, postProcessQueueListDRIn);
                    if (0 < objPostProcessQueListDROut.getStrActionInfoSeq().size()) {
                        log.info("strActionInfoSeq.length() : {}", objPostProcessQueListDROut.getStrActionInfoSeq().size());
                        bCastMultiLotTypeUpdate = true;
                    }
                }

                //DSN000050720 Add End

                //----------------------------------------
                //  Delete Post Process Queue by D_KEY
                //----------------------------------------
                //DSN000050720 Add End
                //----------------------------------------
                //  Delete Post Process Queue by D_KEY
                //----------------------------------------
                log.info("# Delete Post Process Queue by D_KEY {}", postProcessDo.getDkey());

                //DSIV00000201                pptPostProcessActionInfoSequence strPostProcessActionInfoSeq;
                List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq = Lists.newArrayList();
                Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
                postProcessActionInfo.setDKey(postProcessDo.getDkey());
                postProcessActionInfo.setSequenceNumber(postProcessDo.getSeqNo());
                strPostProcessActionInfoSeq.add(postProcessActionInfo);
                postProcessMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, strPostProcessActionInfoSeq);
            }

            if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD, durableCategory)) {
                cimReticlePod = baseCoreFactory.getBO(CimReticlePod.class, strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());
                cimReticlePod.makePostProcessFlagOff();
            } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE, durableCategory)) {
                cimProcessDurable = baseCoreFactory.getBO(CimProcessDurable.class, strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());
                cimProcessDurable.makePostProcessFlagOff();
            } else {
                //DNS000096126 Add End
                //DNS000096126 Indentation Start
                //-------------------------
                //  Get Cassette Object
                //-------------------------
                cimCassette = baseCoreFactory.getBO(CimCassette.class, strLotCassettePostProcessForceDeleteReqInParams.getCassetteID());
                //----------------------------------------------
                //  Set InPostProcessFlag of Cassette to OFF
                //----------------------------------------------
                cimCassette.makePostProcessFlagOff();
                log.info("# Set InPostProcessFlag of Cassette to OFF");
                //DSN000096126 Indentation End
            }  //DSN000096126
        }

        //DSN000050720 Add Start
        if (bCastMultiLotTypeUpdate) {

            ObjectIdentifier cassetteID = null;
            //Get cassette ID

            if (!ObjectIdentifier.isEmpty(strLotCassettePostProcessForceDeleteReqInParams.getLotID()))
            {
                log.info("lotID is specified");
                cassetteID = lotMethod.lotCassetteGet(objCommon, strLotCassettePostProcessForceDeleteReqInParams.getLotID());
            }
            else if ( !ObjectIdentifier.isEmpty(strLotCassettePostProcessForceDeleteReqInParams.getCassetteID()) )
            {
                log.info("cassetteID is specified");
                cassetteID = strLotCassettePostProcessForceDeleteReqInParams.getCassetteID();
            }

            if (!ObjectIdentifier.isEmpty(cassetteID)) {
                log.info("cassetteID {}", ObjectIdentifier.fetchValue(cassetteID));
                cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
            }
        }

    }

    @Override
    public Results.DurableHoldListAttributesResult durableFillInTxPDQ025(Infos.ObjCommon objCommon, Params.StrDurableFillInTxPDQ025InParams strDurableFillInTxPDQ025InParams) {
        log.info("PPTManager_i::durable_FillInTxPDQ025");
        List<DurableDTO.PosDurableHoldRecord> posDurableHoldRecords = Lists.newArrayList();
        if (CimStringUtils.equals(SP_DURABLECAT_CASSETTE, strDurableFillInTxPDQ025InParams.getDurableCategory())) {
            log.info("durableCategory is Cassette");
            CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, strDurableFillInTxPDQ025InParams.getDurableID());
            posDurableHoldRecords = cassette.allHoldRecords();
        }
        else if (CimStringUtils.equals(SP_DURABLECAT_RETICLEPOD, strDurableFillInTxPDQ025InParams.getDurableCategory()))
        {
            log.info("durableCategory is ReticlePod");
            CimReticlePod reticlePod = baseCoreFactory.getBO(CimReticlePod.class, strDurableFillInTxPDQ025InParams.getDurableID());
            posDurableHoldRecords = reticlePod.allHoldRecords();
        }
        else if (CimStringUtils.equals(SP_DURABLECAT_RETICLE, strDurableFillInTxPDQ025InParams.getDurableCategory()))
        {
            log.info("durableCategory is Reticle");
            CimProcessDurable processDurable = baseCoreFactory.getBO(CimProcessDurable.class, strDurableFillInTxPDQ025InParams.getDurableID());
            posDurableHoldRecords = processDurable.allHoldRecords();
        }

        Results.DurableHoldListAttributesResult result = new Results.DurableHoldListAttributesResult();

        log.info("PPTManager_i::durable_FillInTxPDQ025 aHoldRecordSequence->length() {}", posDurableHoldRecords.size());
        if (CollectionUtils.isEmpty(posDurableHoldRecords)) {
            return result;
        }

        CimCategory category = codeManager.findCategoryNamed(SP_REASONCAT_DURABLEHOLD);
        Validations.check(Objects.isNull(category), retCodeConfig.getNotFoundCategory());

        List<Results.DurableHoldAttributeResult> aPerson_is_not_nil = posDurableHoldRecords.stream().map(posDurableHoldRecord -> {

            Results.DurableHoldAttributeResult durableHoldAttributeResult = new Results.DurableHoldAttributeResult();
            durableHoldAttributeResult.setHoldType(posDurableHoldRecord.getHoldType());
            durableHoldAttributeResult.setReasonCodeID(posDurableHoldRecord.getReasonCode());
            durableHoldAttributeResult.setRelatedDurableID(posDurableHoldRecord.getRelatedDurable());
            durableHoldAttributeResult.setUserID(posDurableHoldRecord.getHoldPerson());
            durableHoldAttributeResult.setHoldTimeStamp(posDurableHoldRecord.getHoldTimeStamp());
            durableHoldAttributeResult.setResponsibleRouteID(posDurableHoldRecord.getResponsibleRouteID());
            durableHoldAttributeResult.setResponsibleOperationNumber(posDurableHoldRecord.getResponsibleOperationNumber());
            durableHoldAttributeResult.setResponsibleOperationName(posDurableHoldRecord.getResponsibleOperationName());
            durableHoldAttributeResult.setClaimMemo(posDurableHoldRecord.getHoldClaimMemo());

            CimCode cimCode = baseCoreFactory.getBO(CimCode.class, posDurableHoldRecord.getReasonCode());
            Validations.check(Objects.isNull(cimCode), retCodeConfig.getNotFoundCode());
            durableHoldAttributeResult.setCodeDescription(cimCode.getDescription());
            CimPerson aPerson = null;

            if (ObjectIdentifier.isEmpty(posDurableHoldRecord.getHoldPerson())) {
                aPerson = personManager.findPersonNamed(posDurableHoldRecord.getHoldPerson().getValue());
            }

            if (Objects.nonNull(aPerson)) {
                log.info("aPerson is not nil");
                durableHoldAttributeResult.setUserName(aPerson.getFullName());
            } else {
                durableHoldAttributeResult.setUserName("");
            }

            if (!posDurableHoldRecord.getResponsibleOperationFlag()) {
                durableHoldAttributeResult.setResponsibleOperationMark(SP_RESPONSIBLEOPERATION_CURRENT);
            } else {
                durableHoldAttributeResult.setResponsibleOperationMark(SP_RESPONSIBLEOPERATION_PREVIOUS);
            }


            return durableHoldAttributeResult;
        }).collect(Collectors.toList());

        Results.DurableHoldListAttributesResult durableHoldListAttributesResult = new Results.DurableHoldListAttributesResult();
        durableHoldListAttributesResult.setDurableHoldListAttributesResults(aPerson_is_not_nil);
        return durableHoldListAttributesResult;
    }
}
