package com.fa.cim.method.impl;


import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.nonruntime.CimFIOwnerChangeDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IOwnerMethod;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.restrict.CimRestriction;
import com.fa.cim.newcore.dto.global.GlobalDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/7/29 11:08
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class OwnerMethod implements IOwnerMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private ObjectMethod objectMethod;

    @Autowired
    private BaseCoreFactory genericCoreFactory;

    @Override
    public Outputs.ObjOwnerChangeDefinitionGetDROut ownerChangeDefinitionGetDR(Infos.ObjCommon objCommon) {
        Outputs.ObjOwnerChangeDefinitionGetDROut out = new Outputs.ObjOwnerChangeDefinitionGetDROut();
        int extendLen = 500;
        int tDefLen = extendLen;
        int tDefObjLen = extendLen;
        int tAllClassLen = extendLen;
        int tAllTableLen = extendLen;
        int tAllTblColLen = extendLen;
        int tAllClsTbLen = extendLen;
        int defCount = 0;
        int defObjCount = 0;
        int allClassCount = 0;
        int allTableCount = 0;
        int allTblColCount = 0;
        int allClsTblCount = 0;
        int defIndex = 0;
        int defTblIndex = 0;
        int defColIndex = 0;
        int defUDataIndex = 0;

        List<Infos.OwnerChangeDefinition> strOwnerChgDefSeq  = new ArrayList<>();
        List<Infos.OwnerChangeDefObjDefinition> strOwnerChangeDefObjDefinitionSeq = new ArrayList<>();
        List<String> allClassNames = new ArrayList<>();
        List<String> allTableNames = new ArrayList<>();
        List<String> allTableAndColumNames = new ArrayList<>();
        List<String> allClassAndTableNames = new ArrayList<>();
        List<Infos.OwnerChangeTableDefinition> dummyTblDefs = new ArrayList<>();
        List<Infos.OwnerChangeColumnDefinition> dummyColDefs = new ArrayList<>();
        List<Infos.UserData> dummyUdataDefs = new ArrayList<>();


        //----------------------------------------------------
        // CURSOR for searching OCOWNERCHG
        //----------------------------------------------------
        log.info("SELECT SQL FROM OCOWNERCHG");
        CimFIOwnerChangeDO cimFIOwnerChangeExam = new CimFIOwnerChangeDO();
        cimFIOwnerChangeExam.setUpdateFlag(1);
        List<CimFIOwnerChangeDO> query = cimJpaRepository.findAll(Example.of(cimFIOwnerChangeExam)).stream()
                .sorted().collect(Collectors.toList());
        //----------------------------------------------------
        // FW Table Definition
        //----------------------------------------------------
        for (CimFIOwnerChangeDO cimFIOwnerChangeDO : query) {
            if (CimStringUtils.equals(BizConstant.SP_OWNERCHANGEDEFINITIONTABLETYPE_FW,cimFIOwnerChangeDO.getTableType())){
                log.info("FW Table Definition");
                //-------------------------------------------
                // Search table infomation
                //-------------------------------------------
                Boolean existFlag = false;
                int targetTblInfoIdx = 0;
                for (targetTblInfoIdx = 0; CimStringUtils.length(getTargetTableInfo(targetTblInfoIdx,TARGET_TABLENAME)) != 0; targetTblInfoIdx++) {
                    if (CimStringUtils.equals(cimFIOwnerChangeDO.getTableName(),getTargetTableInfo(targetTblInfoIdx,TARGET_TABLENAME))
                            && CimStringUtils.equals(cimFIOwnerChangeDO.getColumnName(),getTargetTableInfo(targetTblInfoIdx,TARGET_COLUMNNAME))){
                        existFlag = true;
                        break;
                    }
                }
                //-------------------------------------------
                // No infomation
                //-------------------------------------------
                Validations.check(CimBooleanUtils.isFalse(existFlag), new OmCode(3111,"Definition is invalid. Please call system engineer."));
                //SET_OWNERCHG_TARGET_DEFINITION start
                setOwnerChangeTargetDefinition(getTargetTableInfo(targetTblInfoIdx,TARGET_CLASSNAME),
                        getTargetTableInfo(targetTblInfoIdx,TARGET_TABLENAME),
                        getTargetTableInfo(targetTblInfoIdx,TARGET_OBJREF),
                        "",
                        getTargetTableInfo(targetTblInfoIdx,TARGET_COLUMNNAME),
                        existFlag,
                        strOwnerChgDefSeq,
                        dummyTblDefs,
                        dummyColDefs,
                        dummyUdataDefs
                        );
                //SET_OWNERCHG_TARGET_DEFINITION end
                setAllList(getTargetTableInfo(targetTblInfoIdx,TARGET_CLASSNAME),allClassNames,allClassCount,tAllClassLen,existFlag);
                setAllList(getTargetTableInfo(targetTblInfoIdx,TARGET_TABLENAME),allTableNames,allTableCount,tAllTableLen,existFlag);
                setAllListValue2(getTargetTableInfo(targetTblInfoIdx,TARGET_TABLENAME),getTargetTableInfo(targetTblInfoIdx,TARGET_COLUMNNAME),allTableAndColumNames,allTblColCount,tAllTblColLen,existFlag);
                setAllListValue2(getTargetTableInfo(targetTblInfoIdx,TARGET_CLASSNAME),getTargetTableInfo(targetTblInfoIdx,TARGET_TABLENAME),allClassAndTableNames,allClsTblCount,tAllClsTbLen,existFlag);
            }
            //----------------------------------------------------
            // User Data Table Definition
            //----------------------------------------------------
            else if (CimStringUtils.equals(BizConstant.SP_OWNERCHANGEDEFINITIONTABLETYPE_UDATA,cimFIOwnerChangeDO.getTableType())){
                log.info("User Data Table Definition");
                //-------------------------------------------
                // Search table infomation
                //-------------------------------------------
                Boolean existFlag = false;
                int udataTblInfoIdx = 0;
                for (udataTblInfoIdx = 0; CimStringUtils.length(getUdataTableInfo(udataTblInfoIdx,UDATA_TABLENAME)) != 0; udataTblInfoIdx++) {
                    if (CimStringUtils.equals(getUdataTableInfo(udataTblInfoIdx,UDATA_TABLENAME),cimFIOwnerChangeDO.getTableName())){
                        existFlag = true;
                        break;
                    }
                }
                //-------------------------------------------
                // No infomation
                //-------------------------------------------
                Validations.check(CimBooleanUtils.isFalse(existFlag), new OmCode(3111,"Definition is invalid. Please call system engineer."));
                //SET_OWNERCHG_TARGET_DEFINITION_UDATA start
                String theClassName = getUdataTableInfo(udataTblInfoIdx, UDATA_CLASSNAME);
                String theTableName = getUdataTableInfo(udataTblInfoIdx, UDATA_TABLENAME);
                String theObjRef = getUdataTableInfo(udataTblInfoIdx, UDATA_PARENTOBJREF);
                String theParentTable = getUdataTableInfo(udataTblInfoIdx, UDATA_PARENTTABLE);
                String theColumnName = cimFIOwnerChangeDO.getColumnName();
                String theUdataName = cimFIOwnerChangeDO.getUdataName();
                String theUdataOrig = cimFIOwnerChangeDO.getUdataOrig();
                setOwnerChangeTargetDefinitionUData(theClassName,
                        theTableName,
                        theObjRef,
                        theParentTable,
                        theColumnName,
                        theUdataName,
                        theUdataOrig,
                        existFlag,
                        strOwnerChgDefSeq,
                        dummyTblDefs,
                        dummyColDefs,
                        dummyUdataDefs);
                setAllList(theClassName,allClassNames,allClassCount,tAllClassLen,existFlag);
                setAllList(theTableName,allTableNames,allTableCount,tAllTableLen,existFlag);
                setAllListValue2(theTableName,theColumnName,allTableAndColumNames,allTblColCount,tAllTblColLen,existFlag);
                setAllListValue2(theClassName,theTableName,allClassAndTableNames,allClsTblCount,tAllClsTbLen,existFlag);
            }
            //----------------------------------------------------
            // Outside FW table Definition
            //----------------------------------------------------
            else if (CimStringUtils.equals(BizConstant.SP_OWNERCHANGEDEFINITIONTABLETYPE_OTHER,cimFIOwnerChangeDO.getTableType())){
                log.info("FS Table Definition");
                //-------------------------------------------
                // Add definition
                //-------------------------------------------
                Infos.OwnerChangeDefObjDefinition ownerChangeDefObjDefinition = new Infos.OwnerChangeDefObjDefinition();
                strOwnerChangeDefObjDefinitionSeq.add(defObjCount,ownerChangeDefObjDefinition);
                ownerChangeDefObjDefinition.setSeqNo(CimNumberUtils.longValue(cimFIOwnerChangeDO.getSequenceNumber()));
                ownerChangeDefObjDefinition.setCommitFlag(CimNumberUtils.longValue(cimFIOwnerChangeDO.getCommitFlag()));
                ownerChangeDefObjDefinition.setTableName(cimFIOwnerChangeDO.getTableName());
                ownerChangeDefObjDefinition.setColumnName(cimFIOwnerChangeDO.getColumnName());
                ownerChangeDefObjDefinition.setColumnObjRef(cimFIOwnerChangeDO.getColumnObj());
                ownerChangeDefObjDefinition.setKeys(cimFIOwnerChangeDO.getKeys());
                defObjCount++;
                Boolean existFlag = false;
                setAllList(cimFIOwnerChangeDO.getTableName(),allTableNames,allTableCount,tAllTableLen,existFlag);
                setAllListValue2(cimFIOwnerChangeDO.getTableName(),cimFIOwnerChangeDO.getColumnName(),allTableAndColumNames,allTblColCount,tAllTblColLen,existFlag);
            }
        }
        out.setStrOwnerChangeDefinitionSeq(strOwnerChgDefSeq);
        out.setStrOwnerChangeDefObjDefinitionSeq(strOwnerChangeDefObjDefinitionSeq);
        out.setAllClassNames(allClassNames);
        out.setAllTableNames(allTableNames);
        out.setAllTableAndColumNames(allTableAndColumNames);
        out.setAllClassAndTableNames(allClassAndTableNames);
        return out;
    }
    private static void setOwnerChangeTargetDefinitionUData(String theClassName,String theTableName,String theObjRef,String theParentTable,String theColumnName,String theUdataName, String theUdataOrig,Boolean existFlag,List<Infos.OwnerChangeDefinition> strOwnerChgDefSeq,List<Infos.OwnerChangeTableDefinition> dummyTblDefs,List<Infos.OwnerChangeColumnDefinition> dummyColDefs ,List<Infos.UserData> dummyUdataDefs){
        setOwnerChangeTargetDefinition(theClassName,
                theTableName,
                theObjRef,
                theParentTable,
                theColumnName,
                existFlag,
                strOwnerChgDefSeq,
                dummyTblDefs,
                dummyColDefs,
                dummyUdataDefs);
        /*-------------------------------------------*/
        /* Search same udata                         */
        /*-------------------------------------------*/
        existFlag = false;
        for (int i = 0; i < CimArrayUtils.getSize(strOwnerChgDefSeq.get(0).getStrOwnerChangeTableDefinitionSeq().get(0).getStrOwnerChangeColumnDefinitionSeq().get(0).getStrUserDataSeq()); i++) {
            List<Infos.UserData> strUserDataSeq = strOwnerChgDefSeq.get(0).getStrOwnerChangeTableDefinitionSeq().get(0).getStrOwnerChangeColumnDefinitionSeq().get(0).getStrUserDataSeq();
            if (CimStringUtils.equals(theUdataName,strUserDataSeq.get(i).getName())
                    && CimStringUtils.equals(theUdataOrig,strUserDataSeq.get(i).getOriginator())){
                existFlag = true;
                break;
            }
        }
        /*-------------------------------------------*/
        /* Add udata to definition                   */
        /*-------------------------------------------*/
        if (CimBooleanUtils.isFalse(existFlag)){
            Infos.UserData userData = new Infos.UserData();
            strOwnerChgDefSeq.get(0).getStrOwnerChangeTableDefinitionSeq().get(0).getStrOwnerChangeColumnDefinitionSeq().get(0).getStrUserDataSeq().add(userData);
            userData.setName(theTableName);
            userData.setOriginator(theUdataOrig);
            userData.setValue("");
            userData.setType("");
        }
    }
    private static void setOwnerChangeTargetDefinition(String theClassName,String theTableName,String theObjRef,String theParentTable,String theColumnName,Boolean existFlag,List<Infos.OwnerChangeDefinition> strOwnerChgDefSeq,List<Infos.OwnerChangeTableDefinition> dummyTblDefs,List<Infos.OwnerChangeColumnDefinition> dummyColDefs ,List<Infos.UserData> dummyUdataDefs){
        /*-------------------------------------------*/
        /* Search same class                         */
        /*-------------------------------------------*/
        Boolean existFlags = false;
        List<Infos.OwnerChangeTableDefinition> dummyTblDefList = new ArrayList<>();
        List<Infos.OwnerChangeColumnDefinition> dummyColDefList = new ArrayList<>();
        List<Infos.UserData> dummyUdataDefList = new ArrayList<>();
        int defCount = CimArrayUtils.getSize(strOwnerChgDefSeq);
        int defIndex = 0;
        for (defIndex = 0; defIndex < defCount; defIndex++) {
            if (CimStringUtils.equals(theClassName,strOwnerChgDefSeq.get(defIndex).getClassName())){
                existFlags = true;
                break;
            }
        }
        /*-------------------------------------------*/
        /* Add class to definition                   */
        /*-------------------------------------------*/
        if (CimBooleanUtils.isFalse(existFlags)){
            log.info("SET_OWNERCHG_TARGET_DEFINITION", "Add class to definition,{}",theClassName);
            Infos.OwnerChangeDefinition ownerChangeDefinition = new Infos.OwnerChangeDefinition();
            strOwnerChgDefSeq.add(defCount,ownerChangeDefinition);
            ownerChangeDefinition.setClassName(theClassName);
            ownerChangeDefinition.setStrOwnerChangeTableDefinitionSeq(dummyTblDefList);
            defIndex = defCount;
            defCount++;
        }
        /*-------------------------------------------*/
        /* Search same table                         */
        /*-------------------------------------------*/
        existFlags = false;
        int defTblIndex = 0;
        for (defTblIndex = 0; defTblIndex < CimArrayUtils.getSize(strOwnerChgDefSeq.get(defIndex).getStrOwnerChangeTableDefinitionSeq()); defTblIndex++) {
            if (CimStringUtils.equals(theTableName,strOwnerChgDefSeq.get(defIndex).getStrOwnerChangeTableDefinitionSeq().get(defTblIndex).getTableName())){
                existFlags = true;
                break;
            }
        }
        /*-------------------------------------------*/
        /* Add table to definition                   */
        /*-------------------------------------------*/
        if (CimBooleanUtils.isFalse(existFlags)){
            log.info("SET_OWNERCHG_TARGET_DEFINITION", "Add table to definition,{}",theTableName);
            Infos.OwnerChangeTableDefinition ownerChangeTableDefinition = new Infos.OwnerChangeTableDefinition();
            strOwnerChgDefSeq.get(defIndex).getStrOwnerChangeTableDefinitionSeq().add(ownerChangeTableDefinition);
            ownerChangeTableDefinition.setTableName(theTableName);
            ownerChangeTableDefinition.setObjRefColumn(theObjRef);
            ownerChangeTableDefinition.setParentTable(theParentTable);
            ownerChangeTableDefinition.setStrOwnerChangeColumnDefinitionSeq(dummyColDefList);
        }
        /*-------------------------------------------*/
        /* Search same column                        */
        /*-------------------------------------------*/
        existFlags = false;
        for (int defColIndex = 0; defColIndex < CimArrayUtils.getSize(strOwnerChgDefSeq.get(defIndex).getStrOwnerChangeTableDefinitionSeq().get(defTblIndex).getStrOwnerChangeColumnDefinitionSeq()); defTblIndex++) {
            if (CimStringUtils.equals(theColumnName,strOwnerChgDefSeq.get(defIndex).getStrOwnerChangeTableDefinitionSeq().get(defTblIndex).getStrOwnerChangeColumnDefinitionSeq().get(defColIndex).getColumnName())){
                existFlags = true;
                break;
            }
        }
        /*-------------------------------------------*/
        /* Add column to definition                  */
        /*-------------------------------------------*/
        if (CimBooleanUtils.isFalse(existFlags)) {
            List<Infos.OwnerChangeColumnDefinition> ownerChangeColumnDefinitionList = strOwnerChgDefSeq.get(defIndex).getStrOwnerChangeTableDefinitionSeq().get(defTblIndex).getStrOwnerChangeColumnDefinitionSeq();
            Infos.OwnerChangeColumnDefinition ownerChangeColumnDefinition = new Infos.OwnerChangeColumnDefinition();
            ownerChangeColumnDefinitionList.add(ownerChangeColumnDefinition);
            ownerChangeColumnDefinition.setColumnName(theColumnName);
            ownerChangeColumnDefinition.setStrUserDataSeq(dummyUdataDefList);
        }
    }

    private static void setAllList(String theAddValue,List<String> theAllSeq,int theAllSeqCount, int theAllSeqTotalLen, Boolean existFlag){
        if (CimStringUtils.isNotEmpty(theAddValue)){
            Boolean existFlags = false;
            for (int index = 0; index < CimArrayUtils.getSize(theAllSeq); index++) {
                if (CimStringUtils.equals(theAddValue,theAllSeq.get(index))){
                    existFlags = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(existFlags)){
                log.info("SET_ALL_LIST", "Add all list");
                theAllSeq.add(theAddValue);
                theAllSeqCount++;
            }
        }
    }

    private static void setAllListValue2(String theAddValue1,String theAddValue2,List<String> theAllSeq, int theAllSeqCount,int theAllSeqTotalLen,Boolean exsitFlag){
        if (CimStringUtils.isNotEmpty(theAddValue1) && CimStringUtils.isNotEmpty(theAddValue2)){
            String keyString = new String();
            keyString += theAddValue1;
            keyString += BizConstant.SP_KEY_SEPARATOR_DOT;
            keyString += theAddValue2;
            setAllList(keyString,theAllSeq,theAllSeqCount,theAllSeqTotalLen,exsitFlag);
        }
    }


    private static String[][] targetTableInfo = {
            {"OMCARRIER",        BizConstant.SP_CLASSNAME_POSCASSETTE,         "RSV_USER_ID",     "CARRIER_RKEY"},
            {"OMPDRBL",        BizConstant.SP_CLASSNAME_POSRETICLE,          "RSV_USER_ID",     "PDRBL_RKEY"},
            {"OMPDRBL",        BizConstant.SP_CLASSNAME_POSRETICLE,          "XFER_RSV_USER_ID",  "PDRBL_RKEY"},
            {"OMRTCLPOD",     BizConstant.SP_CLASSNAME_POSRETICLEPOD,       "XFER_RSV_USER_ID",  "RTCLPOD_RKEY"},
            {"OMEQPMEMO",     BizConstant.SP_CLASSNAME_POSMACHINENOTE,      "OWNER_ID",          ""},
            {"OMPRORDER",     BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST,   "LOT_OWNER_ID",      "ID"},
            {"OMPRORDER",     BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST,   "LAST_TRX_USER_ID",     "ID"},
            {"OMCJ",     BizConstant.SP_CLASSNAME_POSCONTROLJOB,       "OWNER_ID",          "CJ_RKEY"},
            {"OMLOT",         BizConstant.SP_CLASSNAME_POSLOT,              "LOT_OWNER_ID",      "LOT_RKEY"},
            {"OMRESTRICT",    BizConstant.SP_CLASSNAME_POSENTITYINHIBIT,    "OWNER_ID",          "ID"},
            {"OMFACTORYMEMO", BizConstant.SP_CLASSNAME_POSFACTORYNOTE,      "OWNER_ID",          "MEMO_RKEY"},
            {"OMLOTCOMMENT",  BizConstant.SP_CLASSNAME_POSLOTCOMMENT,       "OWNER_ID",          "MEMO_RKEY"},
            {"OMLOTMEMO",     BizConstant.SP_CLASSNAME_POSLOTNOTE,          "OWNER_ID",          "MEMO_RKEY"},
            {"OMLOTOPEMEMO",  BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE, "OWNER_ID",          "MEMO_RKEY"},
            {"",              "",                               "",                  ""}
    };

    public static String getTargetTableInfo(Integer rowNum, Integer columeNum){
        //targetTableInfo.length    -----行数
        //targetTableInfo[0].length -----列数
        if(rowNum == null || columeNum == null || rowNum > targetTableInfo.length || columeNum > targetTableInfo[0].length){
            return null;
        }
        return targetTableInfo[rowNum][columeNum];
    }

    private static final int TARGET_TABLENAME = 0;
    private static final int TARGET_CLASSNAME = 1;
    private static final int TARGET_COLUMNNAME = 2;
    private static final int TARGET_OBJREF = 3;

    private static String[][] udataTableInfo = {
        {"OMBAY_CDA",       BizConstant.SP_CLASSNAME_POSAREA,                          "OMBAY",         "OMBAY_RKEY"},
        {"OMBAYGRP_CDA",    BizConstant.SP_CLASSNAME_POSAREAGROUP,                     "OMBAYGRP",      "OMBAYGRP_RKEY"},
        {"OMBANK_CDA",       BizConstant.SP_CLASSNAME_POSBANK,                          "OMBANK",         "OMBANK_RKEY"},
        {"OMBINSETUP_CDA",     BizConstant.SP_CLASSNAME_POSBINDEFINITION,                 "OMBINSETUP",       "BINDEF_RKEY"},
        {"OMTESTBINSPEC_CDA",    BizConstant.SP_CLASSNAME_POSBINSPECIFICATION,              "OMTESTBINSPEC",      "BINSPEC_RKEY"},
        {"OMBOM_CDA",        BizConstant.SP_CLASSNAME_POSBOM,                           "OMBOM",          "BOM_RKEY"},
        {"OMCALDR_CDA",   BizConstant.SP_CLASSNAME_POSCALENDARDATE,                  "OMCALDR",     "OMCALDR_RKEY"},
        {"OMCARRIER_CDA",       BizConstant.SP_CLASSNAME_POSCASSETTE,                      "OMCARRIER",         "CARRIER_RKEY"},
        {"OMCODE_CDA",       BizConstant.SP_CLASSNAME_POSCODE,                          "OMCODE",         "CODE_RKEY"},
        {"OMCJ_CDA",    BizConstant.SP_CLASSNAME_POSCONTROLJOB,                    "OMCJ",      "CJ_RKEY"},
        {"OMCUSTOMER_CDA",   BizConstant.SP_CLASSNAME_POSCUSTOMER,                      "OMCUSTOMER",     "CUSTOMER_OBJ"},
        {"OMCUSTPROD_CDA",   BizConstant.SP_CLASSNAME_POSCUSTOMERPRODUCT,               "OMCUSTPROD",     "CUSTPROD_OBJ"},
        {"OMEDCPLAN_CDA",      BizConstant.SP_CLASSNAME_POSDATACOLLECTIONDEFINITION,      "OMEDCPLAN",        "DCDEF_OBJ"},
        {"OMEDCSPEC_CDA",     BizConstant.SP_CLASSNAME_POSDATACOLLECTIONSPECIFICATION,   "OMEDCSPEC",       "DCSPEC_OBJ"},
        {"OMWNXT_CDA",       BizConstant.SP_CLASSNAME_POSDISPATCHER,                    "OMWNXT",         "OMWNXT_OBJ"},
        {"OMPDRBL_CDA",       BizConstant.SP_CLASSNAME_POSRETICLE,                       "OMPDRBL",         "PDRBL_RKEY"},
        {"OMPDRBLGRP_CDA",    BizConstant.SP_CLASSNAME_POSPROCESSDURABLECAPABILITY,      "OMPDRBLGRP",      "PDRBL_GRP_RKEY"},
        {"OME10STATE_CDA",   BizConstant.SP_CLASSNAME_POSE10STATE,                      "OME10STATE",     "E10STATE_RKEY"},
        {"OMRESTRICT_CDA",   BizConstant.SP_CLASSNAME_POSENTITYINHIBIT,                 "OMRESTRICT",     "RESTRICT_RKEY"},
        {"OMEQP_CDA",        BizConstant.SP_CLASSNAME_POSMACHINE,                       "OMEQP",          "EQP_RKEY"},
        {"OMEQPCTNR_CDA",     BizConstant.SP_CLASSNAME_POSMACHINECONTAINER,               "OMEQPCTNR",       "EQPCTN_RKEY"},
        {"OMEQPCTNRPOS_CDA",  BizConstant.SP_CLASSNAME_POSMACHINECONTAINERPOSITION,      "OMEQPCTNRPOS",    "EQPCTNPST_RKEY"},
        {"OMEQPMEMO_CDA",      BizConstant.SP_CLASSNAME_POSMACHINENOTE,                   "OMEQPMEMO",      ""},
        {"OMEQPOPMANUAL_CDA",    BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONPROCEDURE,   "OMEQPOPMANUAL",  "MEMO_RKEY"},
        {"OMEQPST_CDA",      BizConstant.SP_CLASSNAME_POSMACHINESTATE,                  "OMEQPST",        "EQPSTATE_RKEY"},
        {"OMFACTORYMEMO_CDA",  BizConstant.SP_CLASSNAME_POSFACTORYNOTE,                   "OMFACTORYMEMO",  "NOTE_OBJ"},
        {"OMFLOWB_CDA",  BizConstant.SP_CLASSNAME_POSFLOWBATCH,                     "OMFLOWB",    "OMFLOWB_OBJ"},
        {"OMFLOWBDISP_CDA",      BizConstant.SP_CLASSNAME_POSFLOWBATCHDISPATCHER,           "OMFLOWBDISP",        "OMFLOWBDISP_OBJ"},
        {"OMFRWK_CDA",      BizConstant.SP_CLASSNAME_POSFUTUREREWORKREQUEST,           "OMFRWK",        "FUTURE_REWORK_RKEY"},
        {"OMLOT_CDA",          BizConstant.SP_CLASSNAME_POSLOT,                           "OMLOT",          "LOT_RKEY"},
        {"OMLOTCOMMENT_CDA", BizConstant.SP_CLASSNAME_POSLOTCOMMENT,                    "OMLOTCOMMENT",   "NOTE_RKEY"},
        {"OMLOTFAMILY_CDA",  BizConstant.SP_CLASSNAME_POSLOTFAMILY,                     "OMLOTFAMILY",    "LOTFAMILY_RKEY"},
        {"OMLOTMEMO_CDA",    BizConstant.SP_CLASSNAME_POSLOTNOTE,                       "OMLOTMEMO",      "MEMO_RKEY"},
        {"OMLOTOPEMEMO_CDA", BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE,              "OMLOTOPEMEMO",   "MEMO_RKEY"},
        {"OMLOTOPEPLAN_CDA", BizConstant.SP_CLASSNAME_POSLOTOPERATIONSCHEDULE,          "OMLOTOPEPLAN",   "LOT_OPE_PLAN_RKEY"},
        {"OMLOTPLAN_CDA",    BizConstant.SP_CLASSNAME_POSLOTSCHEDULE,                   "OMLOTPLAN",      "LOT_PLAN_RKEY"},
        {"OMLOTTYPE_CDA",    BizConstant.SP_CLASSNAME_POSLOTTYPE,                       "OMLOTTYPE",      "LOTTYPE_RKEY"},
        {"OMLRCP_CDA",       BizConstant.SP_CLASSNAME_POSLOGICALRECIPE,                 "OMLRCP",         "LRCP_RKEY"},
        {"OMMONGRP_CDA", BizConstant.SP_CLASSNAME_POSMONITORGROUP,                  "OMMOGRP",   "MON_GRP_RKEY"},
        {"OMRCP_CDA",       BizConstant.SP_CLASSNAME_POSMACHINERECIPE,                 "OMRCP",         "RECIPE_RKEY"},
        {"OMNOTIFYDEF_CDA",     BizConstant.SP_CLASSNAME_POSMESSAGEDEFINITION,             "NOTIFYDEF",       "NOTIFY_RKEY"},
        {"OMMATLOC_CDA",    BizConstant.SP_CLASSNAME_POSMATERIALLOCATION,                  "OMMATLOC",      "MTRLLOC_RKEY"},
        {"OMEQPOPEMODE_CDA",    BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONMODE,          "OMEQPOPEMODE",      "OPEMODE_RKEY"},
        {"OMPRP_CDA",         BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,             "OMPRP",           "PRP_RKEY"},
        {"OMPRF_CDA",         BizConstant.SP_CLASSNAME_POSPROCESSFLOW,                   "OMPRF",           "PRF_RKEY"},
        {"OMPRFCX_CDA",        BizConstant.SP_CLASSNAME_POSPROCESSFLOWCONTEXT,            "OMPRFCX",          "PRFCX_RKEY"},
        {"OMPSM_CDA",    BizConstant.SP_CLASSNAME_POSPLANNEDSPLITJOB,               "OMPSM",      "PSM_RKEY"},
        {"OMPROPE_CDA",         BizConstant.SP_CLASSNAME_POSPROCESSOPERATION,              "OMPROPE",           "PROPE_RKEY"},
        {"OMPORT_CDA",       BizConstant.SP_CLASSNAME_POSPORTRESOURCE,                  "OMPORT",         "PORT_OBJ"},
        {"OMPRSS_CDA",        BizConstant.SP_CLASSNAME_POSPROCESSOPERATIONSPECIFICATION, "OMPRSS",          "PRSS_RKEY"},
        {"OMPROCRES_CDA",     BizConstant.SP_CLASSNAME_POSPROCESSRESOURCE,               "OMPROCRES",       "PROCRSC_OBJ"},
        {"OMACCESSGRP_CDA",    BizConstant.SP_CLASSNAME_POSPRIVILEGEGROUP,                "OMACCESSGRP",      "ACCESSGRP_RKEY"},
        {"OMPROCRES_CDA",     BizConstant.SP_CLASSNAME_POSPROCESSRESOURCE,               "OMPROCRES",       "PROCRES_RKEY"},
        {"OMACCESSGRP_CDA",    BizConstant.SP_CLASSNAME_POSPRIVILEGEGROUP,                "OMACCESSGRP",      "ACCESSGRP_RKEY"},
        {"OMPRODCAT_CDA",    BizConstant.SP_CLASSNAME_POSPRODUCTCATEGORY,               "OMPRODCAT", "PROD_CATEGORY_OBJ"},
        {"OMPRODFMLY_CDA",    BizConstant.SP_CLASSNAME_POSPRODUCTGROUP,                  "OMPRODFMLY",      "PRODGRP_OBJ"},
        {"OMPRORDER_CDA",    BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST,                "OMPRORDER",      "ID"},
        {"OMPRODINFO_CDA",   BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION,          "OMPRODINFO",     "PRODSPEC_OBJ"},
        {"OMQT_CDA",      BizConstant.SP_CLASSNAME_POSQTIMERESTRICTION,              "OMQT",        "QT_RKEY"},
        {"OMPHYEQPST_CDA",   BizConstant.SP_CLASSNAME_POSRAWMACHINESTATESET,            "OMPHYEQPST",     "PHYEQPST_RKEY"},
        {"OMRTCLPODPORT_CDA",    BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE,        "OMRTCLPODPORT",      "PORT_OBJ"},
        {"OMRTCLPOD_CDA",    BizConstant.SP_CLASSNAME_POSRETICLEPOD,                    "OMRTCLPOD",      "RTCLPOD_RKEY"},
        {"OMRTCLSET_CDA",    BizConstant.SP_CLASSNAME_POSRETICLESET,                    "OMRTCLSET",      "RTCLSET_RKEY"},
        {"OMPCS_CDA",     BizConstant.SP_CLASSNAME_POSSCRIPT,                        "OMPCS",       "PCS_RKEY"},
        {"OMQSAMPLESPEC_CDA",   BizConstant.SP_CLASSNAME_POSSAMPLESPECIFICATION,           "OMQSAMPLESPEC",     "SMPLSPEC_OBJ"},
        {"OMSTAGE_CDA",      BizConstant.SP_CLASSNAME_POSSTAGE,                         "OMSTAGE",        "OMSTAGE_OBJ"},
        {"OMSTAGEGRP_CDA",   BizConstant.SP_CLASSNAME_POSSTAGEGROUP,                    "OMSTAGEGRP",     "OMSTAGEGRP_OBJ"},
        {"OMSTOCKER_CDA",        BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE,                "OMSTOCKER",          ""},
        {"OMNOTIFYMSG_CDA",     BizConstant.SP_CLASSNAME_POSSYSTEMMESSAGECODE,             "OMNOTIFYMSG",   "NOTIFYMSG_RKEY"},
        {"OMTECH_CDA",       BizConstant.SP_CLASSNAME_POSTECHNOLOGY,                    "OMTECH",         "TECH_RKEY"},
        {"OMTSPEC_CDA",   BizConstant.SP_CLASSNAME_POSTESTSPECIFICATION,             "OMTSPEC",     "TESTSPEC_RKEY"},
        {"OMTTYPE_CDA",   BizConstant.SP_CLASSNAME_POSTESTTYPE,                      "OMTTYPE",     "TESTTYPE_RKEY"},
        {"OMUSER_CDA",       BizConstant.SP_CLASSNAME_POSPERSON,                            "OMUSER",         "USER_RKEY"},
        {"OMUSERGRP_CDA",    BizConstant.SP_CLASSNAME_POSUSERGROUP,                     "OMUSERGRP",      "OMUSERGRP_RKEY"},
        {"OMWAFER_CDA",      BizConstant.SP_CLASSNAME_POSWAFER,                         "OMWAFER",        "WAFER_RKEY"},
        {"",                   "",                                            "",               ""}
    };

    public static String getUdataTableInfo(Integer rowNum, Integer columeNum){
        //udataTableInfo.length    -----行数
        //udataTableInfo[0].length -----列数
        if(rowNum == null || columeNum == null || rowNum > udataTableInfo.length || columeNum > udataTableInfo[0].length){
            return null;
        }
        return udataTableInfo[rowNum][columeNum];
    }

    private static final int UDATA_TABLENAME = 0;
    private static final int UDATA_CLASSNAME = 1;
    private static final int UDATA_PARENTTABLE = 2;
    private static final int UDATA_PARENTOBJREF = 3;

    @Override
    public List<CimBO> ownerChangeObjectListGet(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeObjectListGetIn in) {
        ObjectIdentifier ownerID = in.getOwnerID();
        String targetClassName = in.getTargetClassName();
        String targetTableName = in.getTargetTableName();
        String targetColumnName = in.getTargetColumnName();
        List<Infos.HashedInfo> strHashedInfoSeq = in.getStrHashedInfoSeq();
        Infos.OwnerChangeDefinition strOwnerChangeDefinition = in.getStrOwnerChangeDefinition();

        int extendLen = 500;
        int tObjRefLen = extendLen;
        int objRefCnt = 0;
        List<CimBO> aObjRefSeq = new ArrayList<>();

        //object specified
        if (CimStringUtils.isNotEmpty(targetClassName) && CimArrayUtils.getSize(strHashedInfoSeq) != 0){
            //---------------------------------------------
            // Get object
            //---------------------------------------------
            //【step1】 - object_Get
            Inputs.ObjObjectGetIn objObjectGetIn = new Inputs.ObjObjectGetIn();
            objObjectGetIn.setClassName(targetClassName);
            objObjectGetIn.setStrHashedInfoSeq(strHashedInfoSeq);
            log.info("call object_Get()");
            CimBO ObjectGetRetCode = objectMethod.objectGet(objCommon, objObjectGetIn);
            aObjRefSeq.add(objRefCnt,ObjectGetRetCode);
            objRefCnt++;
        }
        // Except object specified
        else {
            for (int i = 0; i < CimArrayUtils.getSize(strOwnerChangeDefinition.getStrOwnerChangeTableDefinitionSeq()); i++) {
                Infos.OwnerChangeTableDefinition strOwnerChangeTableDefinition = strOwnerChangeDefinition.getStrOwnerChangeTableDefinitionSeq().get(i);
                Infos.OwnerChangeColumnDefinition aOwnerChangeColumnDefinition = null;
                if (CimStringUtils.isNotEmpty(targetTableName) && !CimStringUtils.equals(targetTableName,strOwnerChangeTableDefinition.getTableName())){
                    continue;
                }
                if (CimStringUtils.isNotEmpty(targetColumnName)){
                    Boolean existFlag = false;
                    for (int j = 0; j < CimArrayUtils.getSize(strOwnerChangeTableDefinition.getStrOwnerChangeColumnDefinitionSeq()); j++) {
                        if (CimStringUtils.equals(targetColumnName,strOwnerChangeTableDefinition.getStrOwnerChangeColumnDefinitionSeq().get(j).getColumnName())){
                            aOwnerChangeColumnDefinition = strOwnerChangeTableDefinition.getStrOwnerChangeColumnDefinitionSeq().get(j);
                            existFlag = true;
                            break;
                        }
                    }//loop of columnIndex
                    if (CimBooleanUtils.isFalse(existFlag)){
                        continue;
                    }
                }
                //---------------------------------------------
                // Get object list
                //---------------------------------------------
                Inputs.ObjOwnerChangeObjectListGetDRIn input = new Inputs.ObjOwnerChangeObjectListGetDRIn();
                input.setOwnerID(ownerID);
                input.setTableName(strOwnerChangeTableDefinition.getTableName());
                input.setObjRefColumn(strOwnerChangeTableDefinition.getObjRefColumn());
                input.setParentTable(strOwnerChangeTableDefinition.getParentTable());
                if (CimStringUtils.isNotEmpty(targetColumnName)){
                    List<Infos.OwnerChangeColumnDefinition> aOwnerChangeColumnDefinitionSeq = new ArrayList<>();
                    aOwnerChangeColumnDefinitionSeq.add(aOwnerChangeColumnDefinition);
                    input.setStrOwnerChangeColumnDefinitionSeq(aOwnerChangeColumnDefinitionSeq);
                }else {
                    input.setStrOwnerChangeColumnDefinitionSeq(strOwnerChangeTableDefinition.getStrOwnerChangeColumnDefinitionSeq());
                }
                log.info("call ownerChange_objectList_GetDR()");
                //【step2】 - ownerChange_objectList_GetDR
                List<CimBO> ownerChangeObjectListGetDR = this.ownerChangeObjectListGetDR(objCommon,input);
                // Since object is got for every table, the duplicating object is not added to result list.
                for (int k = 0; k < CimArrayUtils.getSize(ownerChangeObjectListGetDR); k++) {
                    Boolean existFlag = false;
                    for (int m = 0; m < objRefCnt; m++) {
                        if (ownerChangeObjectListGetDR.get(k).equals(aObjRefSeq.get(m))){
                            existFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isFalse(existFlag)){
                        aObjRefSeq.add(objRefCnt,ownerChangeObjectListGetDR.get(k));
                    }
                }
            }
        }
        return aObjRefSeq;
    }

    @Override
    public List<CimBO> ownerChangeObjectListGetDR(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeObjectListGetDRIn input) {

        //init
        ObjectIdentifier ownerID = input.getOwnerID();
        String tableName = input.getTableName();
        String objRefColumn = input.getObjRefColumn();
        String parentTable = input.getParentTable();
        List<Infos.OwnerChangeColumnDefinition> strOwnerChangeColumnDefinitionSeq = input.getStrOwnerChangeColumnDefinitionSeq();

        int extendLen = 500;
        int tLen = extendLen;
        int count = 0;

        List<CimBO> stringifiedObjectReferences = new ArrayList<>();
        StringBuilder HV_BUFFER = new StringBuilder();
        String HV_TMPBUFFER = "";
        HV_BUFFER = new StringBuilder("SELECT");

        //----------------------------------------------------
        // Child table
        //----------------------------------------------------
        if (CimStringUtils.isNotEmpty(parentTable)){
            HV_TMPBUFFER = String.format(" TBL2.%s",objRefColumn);
            HV_BUFFER.append(HV_TMPBUFFER);

            HV_TMPBUFFER = String.format(" FROM %s TBL1",tableName);
            HV_BUFFER.append(HV_TMPBUFFER);

            HV_TMPBUFFER = String.format(" , %s TBL2",parentTable);
            HV_BUFFER.append(HV_TMPBUFFER);

            HV_BUFFER.append(" WHERE TBL1.ID = TBL2.REFKEY AND ("); //" WHERE TBL1.D_THESYSTEMKEY = TBL2.D_THESYSTEMKEY AND ("
        }
        //----------------------------------------------------
        // Parent table
        //----------------------------------------------------
        else {
            HV_TMPBUFFER = String.format(" TBL1.%s",objRefColumn);
            HV_BUFFER.append(HV_TMPBUFFER);

            HV_TMPBUFFER = String.format(" FROM %s TBL1",tableName);
            HV_BUFFER.append(HV_TMPBUFFER);

            HV_BUFFER.append(" WHERE (");
        }
        for (int i = 0; i < CimArrayUtils.getSize(strOwnerChangeColumnDefinitionSeq); i++) {
            if (i > 0){
                HV_BUFFER.append(" OR");
            }
            HV_TMPBUFFER = String.format(" ( TBL1.%s",strOwnerChangeColumnDefinitionSeq.get(i).getColumnName());
            HV_BUFFER.append(HV_TMPBUFFER);

            HV_TMPBUFFER = String.format(" = '%s'",ownerID.getValue());
            HV_BUFFER.append(HV_TMPBUFFER);
            //----------------------------------------------------
            // UDATA table
            //----------------------------------------------------
            if (CimArrayUtils.getSize(strOwnerChangeColumnDefinitionSeq.get(i).getStrUserDataSeq()) > 0){
                HV_BUFFER.append(" AND (");
                for (int j = 0; j < CimArrayUtils.getSize(strOwnerChangeColumnDefinitionSeq.get(i).getStrUserDataSeq()); j++) {
                    if (j > 0){
                        HV_BUFFER.append(" OR");
                    }
                    HV_TMPBUFFER = String.format(" ( TBL1.NAME = '%s'",strOwnerChangeColumnDefinitionSeq.get(i).getStrUserDataSeq().get(j).getName());
                    HV_BUFFER.append(HV_TMPBUFFER);

                    HV_TMPBUFFER = String.format(" AND TBL1.ORIG = '%s' )",strOwnerChangeColumnDefinitionSeq.get(i).getStrUserDataSeq().get(j).getOriginator());
                    HV_BUFFER.append(HV_TMPBUFFER);
                }
                HV_BUFFER.append(" ) )");
            }
            //----------------------------------------------------
            // OTHER
            //----------------------------------------------------
            else {
                HV_BUFFER.append(" )");
            }
        }
        HV_BUFFER.append(" )");
        //-------------------------------------------
        // Judge and Convert SQL with Escape Sequence
        //-------------------------------------------
        Boolean bConvertFlag = false;
        String originalSQL = HV_BUFFER.toString();
        List<Object[]> queryResult = cimJpaRepository.query(originalSQL);
        if (!CimObjectUtils.isEmpty(queryResult)){
            for (Object[] objects : queryResult) {
                //right??  EXEC SQL FETCH OWNCHGLIST_C1 INTO :hSTRINGOBJREF;
                CimBO bo = (CimBO) genericCoreFactory.getBO(tableName, objects[objects.length - 1].toString());//ID
                stringifiedObjectReferences.add(bo);
            }
        }
        return stringifiedObjectReferences;
    }


    @Override
    public <T extends CimBO> Boolean ownerChangeUpdate(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeUpdateIn in,T bo) {
        String stringifiedObjectReference = in.getStringifiedObjectReference();
        Infos.OwnerChangeDefinition strOwnerChangeDefinition = in.getStrOwnerChangeDefinition();
        Infos.OwnerChangeReqInParm strOwnerChangeReqInParm = in.getStrOwnerChangeReqInParm();

        //get aToPerson
        com.fa.cim.newcore.bo.person.CimPerson cimToPerson = genericCoreFactory.getBO(com.fa.cim.newcore.bo.person.CimPerson.class, strOwnerChangeReqInParm.getToOwnerID());
        //CimPersonDO aToPerson = personCore.convertObjectIdentifierToEntity(strOwnerChangeReqInParm.getToOwnerID());
        Validations.check(CimObjectUtils.isEmpty(cimToPerson), retCodeConfig.getNotFoundPerson());
        String str = stringifiedObjectReference;
        //未按照源码实现reactivateCIMFWBO(str) 方法得到 anObject ,单独判断
        //TODO:CIMFWBO_var anObject = reactivateCIMFWBO(str);
        //null check for anObject
        /*if (ObjectUtils.isEmpty(anObject)){
            result.setReturnCode(retCodeConfig.getNotFoundObject());
            return result;
        }*/
        String currClass = strOwnerChangeDefinition.getClassName();
        if (CimStringUtils.isNotEmpty(strOwnerChangeReqInParm.getTargetClassName())){
            if (!CimStringUtils.equals(strOwnerChangeReqInParm.getTargetClassName(),currClass)){
                return false;
            }
        }
        Boolean updateFlag = false;
        for (int i = 0; i < CimArrayUtils.getSize(strOwnerChangeDefinition.getStrOwnerChangeTableDefinitionSeq()); i++) {
            Infos.OwnerChangeTableDefinition strOwnerChangeTableDefinition = strOwnerChangeDefinition.getStrOwnerChangeTableDefinitionSeq().get(i);
            String currTable = strOwnerChangeTableDefinition.getTableName();
            if (CimStringUtils.isNotEmpty(strOwnerChangeReqInParm.getTargetTableName())){
                if (!CimStringUtils.equals(strOwnerChangeReqInParm.getTargetTableName(),currTable)){
                    continue;
                }
            }
            for (int j = 0; j < CimArrayUtils.getSize(strOwnerChangeTableDefinition.getStrOwnerChangeColumnDefinitionSeq()); j++) {
                Infos.OwnerChangeColumnDefinition strOwnerChangeColumnDefinition = strOwnerChangeTableDefinition.getStrOwnerChangeColumnDefinitionSeq().get(j);
                String currColumn = strOwnerChangeColumnDefinition.getColumnName();
                if (CimStringUtils.isNotEmpty(strOwnerChangeReqInParm.getTargetColumnName())){
                    if (!CimStringUtils.equals(strOwnerChangeReqInParm.getTargetColumnName(),currColumn)){
                        continue;
                    }
                }
                //1.OWNER_CONFIRMED_AND_CHANGED_BY_RESERVEPERSON   PosCassette
                if (CimStringUtils.equals(currClass, BizConstant.SP_CLASSNAME_POSCASSETTE)
                        && CimStringUtils.equals(currTable,"OMCARRIER")
                        && CimStringUtils.equals(currColumn,"RSV_USER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.durable.CimCassette){
                        com.fa.cim.newcore.bo.durable.CimCassette cimCassette = (com.fa.cim.newcore.bo.durable.CimCassette) bo;
                        if (!CimObjectUtils.isEmpty(cimCassette)){
                            String ownerID = cimCassette.getReservePersonID();
                            if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                Boolean reserved = false;
                                reserved = cimCassette.isReserved();
                                if (CimBooleanUtils.isTrue(reserved)){
                                    cimCassette.unReserve();
                                }
                                cimCassette.reserveFor(cimToPerson);
                                updateFlag = true;
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            strUserDataSets = cimCassette.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimCassette.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //2.OWNER_CONFIRMED_AND_CHANGED_BY_RESERVEPERSON  PosReticle
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSRETICLE)
                        && CimStringUtils.equals(currTable,"OMPDRBL")
                        && CimStringUtils.equals(currColumn,"RSV_USER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.durable.CimProcessDurable){
                        com.fa.cim.newcore.bo.durable.CimProcessDurable cimReticle = (com.fa.cim.newcore.bo.durable.CimProcessDurable) bo;
                        if (!CimObjectUtils.isEmpty(cimReticle)){
                            String ownerID = cimReticle.getReservePersonID();
                            if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                Boolean reserved = false;
                                reserved = cimReticle.isReserved();
                                if (CimBooleanUtils.isTrue(reserved)){
                                    cimReticle.unReserve();
                                }
                                cimReticle.reserveFor(cimToPerson);
                                updateFlag = true;
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            // getUserDataSetNamedAndOrig cimDurableDO
                            strUserDataSets = cimReticle.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimReticle.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //3.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONOBJ  PosReticle
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSRETICLE)
                        && CimStringUtils.equals(currTable,"OMPDRBL")
                        && CimStringUtils.equals(currColumn,"XFER_RSV_USER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.durable.CimProcessDurable){
                        com.fa.cim.newcore.bo.durable.CimProcessDurable cimReticle = (com.fa.cim.newcore.bo.durable.CimProcessDurable) bo;
                        if (!CimObjectUtils.isEmpty(cimReticle)){
                            com.fa.cim.newcore.bo.person.CimPerson aPerson = cimReticle.getTransferReserveUser();
                            if (!CimObjectUtils.isEmpty(aPerson)){
                                String ownerID = aPerson.getIdentifier();
                                if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    cimReticle.setTransferReserveUser(cimToPerson);
                                    updateFlag = true;
                                }
                            }
                        }else {
                           throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimDurableDO
                            strUserDataSets = cimReticle.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimReticle.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //4.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONOBJ  PosReticlePod
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSRETICLEPOD)
                        && CimStringUtils.equals(currTable,"OMRTCLPOD")
                        && CimStringUtils.equals(currColumn,"XFER_RSV_USER_ID")){
                    if (bo instanceof CimReticlePod){
                        CimReticlePod cimReticlePod = (CimReticlePod) bo;
                        if (!CimObjectUtils.isEmpty(cimReticlePod)){
                            com.fa.cim.newcore.bo.person.CimPerson aPerson = cimReticlePod.getTransferReserveUser();
                            if (!CimObjectUtils.isEmpty(aPerson)){
                                String ownerID = aPerson.getIdentifier();
                                if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    cimReticlePod.setTransferReserveUser(cimToPerson);
                                    updateFlag = true;
                                }
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimReticlePodDO
                            strUserDataSets = cimReticlePod.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimReticlePod.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //5.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONID
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSMACHINENOTE) && CimStringUtils.equals(currTable,"OMEQPMEMO") && CimStringUtils.equals(currColumn,"OWNER_ID")){
                    if ( bo instanceof com.fa.cim.newcore.bo.machine.CimMachineNote ){
                        com.fa.cim.newcore.bo.machine.CimMachineNote cimMachineNote = (com.fa.cim.newcore.bo.machine.CimMachineNote) bo;
                        if (!CimObjectUtils.isEmpty(cimMachineNote)){
                            String ownerID = cimMachineNote.getPersonID();
                            if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                //cimMachineNote.setPersonID(cimToPerson);
                                cimMachineNote.setPerson(cimToPerson);
                                updateFlag = true;
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimEquipmentNoteDO
                            strUserDataSets = cimMachineNote.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimMachineNote.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }

                //6.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONOBJ
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST) && CimStringUtils.equals(currTable,"OMPRORDER") && CimStringUtils.equals(currColumn,"LOT_OWNER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.planning.CimProductRequest){
                        com.fa.cim.newcore.bo.planning.CimProductRequest cimProductRequest = (com.fa.cim.newcore.bo.planning.CimProductRequest) bo;
                        if (!CimObjectUtils.isEmpty(cimProductRequest)){
                            com.fa.cim.newcore.bo.person.CimPerson aPerson = cimProductRequest.getLotOwner();
                            if (!CimObjectUtils.isEmpty(aPerson)){
                                String ownerID = aPerson.getIdentifier();
                                if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    cimProductRequest.setLotOwner(cimToPerson);
                                    updateFlag = true;
                                }
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimProductRequestDO
                            strUserDataSets = cimProductRequest.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimProductRequest.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //7.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONID
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST) && CimStringUtils.equals(currTable,"OMPRORDER") && CimStringUtils.equals(currColumn,"LAST_TRX_USER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.planning.CimProductRequest){
                        com.fa.cim.newcore.bo.planning.CimProductRequest cimProductRequest = (com.fa.cim.newcore.bo.planning.CimProductRequest) bo;
                        if (!CimObjectUtils.isEmpty(cimProductRequest)){
                            String ownerID = cimProductRequest.getLastClaimedPersonID();
                            if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                cimProductRequest.setLastClaimedPerson(cimToPerson);
                                updateFlag = true;
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimProductRequestDO
                            strUserDataSets = cimProductRequest.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimProductRequest.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //8.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONOBJ
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSCONTROLJOB)
                        && CimStringUtils.equals(currTable,"OMCJ")
                        && CimStringUtils.equals(currColumn,"OWNER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.product.CimControlJob){
                        com.fa.cim.newcore.bo.product.CimControlJob cimControlJob = (com.fa.cim.newcore.bo.product.CimControlJob) bo;
                        if (!CimObjectUtils.isEmpty(cimControlJob)){
                            com.fa.cim.newcore.bo.person.CimPerson aPerson = cimControlJob.getOwner();
                            if (!CimObjectUtils.isEmpty(aPerson)){
                                String ownerID = aPerson.getIdentifier();
                                if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    cimControlJob.setOwner(cimToPerson);
                                    updateFlag = true;
                                }
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            strUserDataSets = cimControlJob.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimControlJob.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //9.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONOBJ
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSLOT)
                        && CimStringUtils.equals(currTable,"OMLOT")
                        && CimStringUtils.equals(currColumn,"LOT_OWNER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.product.CimLot){
                        CimLot cimLot = (CimLot) bo;
                        if (!CimObjectUtils.isEmpty(cimLot)){
                            com.fa.cim.newcore.bo.person.CimPerson aPerson = cimLot.getLotOwner();
                            if (!CimObjectUtils.isEmpty(aPerson)){
                                String ownerID = aPerson.getIdentifier();
                                if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    cimLot.setLotOwner(cimToPerson);
                                    updateFlag = true;
                                }
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimLotDO
                            strUserDataSets = cimLot.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimLot.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //10.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONOBJ
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSENTITYINHIBIT) && CimStringUtils.equals(currTable,"OMRESTRICT") && CimStringUtils.equals(currColumn,"OWNER_ID")){
                    if (bo instanceof CimRestriction){
                        CimRestriction cimEntityInhibit = (CimRestriction) bo;
                        if (!CimObjectUtils.isEmpty(cimEntityInhibit)){
                            com.fa.cim.newcore.bo.person.CimPerson aPerson = cimEntityInhibit.getOwner();
                            if (!CimObjectUtils.isEmpty(aPerson)){
                                String ownerID = aPerson.getIdentifier();
                                if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    cimEntityInhibit.setOwner(cimToPerson);
                                    updateFlag = true;
                                }
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimEntityInhibitDO
                            strUserDataSets = cimEntityInhibit.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimEntityInhibit.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //11.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONID
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSFACTORYNOTE) && CimStringUtils.equals(currTable,"OMFACTORYMEMO") && CimStringUtils.equals(currColumn,"OWNER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.factory.CimFactoryNote){
                        com.fa.cim.newcore.bo.factory.CimFactoryNote cimFactoryNote = (com.fa.cim.newcore.bo.factory.CimFactoryNote) bo;
                        if (!CimObjectUtils.isEmpty(cimFactoryNote)){
                            String ownerID = cimFactoryNote.getPersonID();
                            if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                //cimFactoryNote.setPersonID(cimToPerson);
                                cimFactoryNote.setPerson(cimToPerson);
                                updateFlag = true;
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimFactoryNoteDO
                            strUserDataSets = cimFactoryNote.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimFactoryNote.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //12.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONID
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSLOTCOMMENT)
                        && CimStringUtils.equals(currTable,"OMLOTCOMMENT")
                        && CimStringUtils.equals(currColumn,"OWNER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.product.CimLotComment){
                        com.fa.cim.newcore.bo.product.CimLotComment cimLotComment = (com.fa.cim.newcore.bo.product.CimLotComment) bo;
                        if (!CimObjectUtils.isEmpty(cimLotComment)){
                            String ownerID = cimLotComment.getPersonID();
                            if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                //cimLotComment.setPersonID(cimToPerson);
                                cimLotComment.setPerson(cimToPerson);
                                updateFlag = true;
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimLotCommentDO
                            strUserDataSets = cimLotComment.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimLotComment.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //13.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONID
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSLOTNOTE)
                        && CimStringUtils.equals(currTable,"OMLOTMEMO")
                        && CimStringUtils.equals(currColumn,"OWNER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.product.CimLotNote) {
                        com.fa.cim.newcore.bo.product.CimLotNote cimLotNote = (com.fa.cim.newcore.bo.product.CimLotNote) bo;
                        if (!CimObjectUtils.isEmpty(cimLotNote)){
                            String ownerID = cimLotNote.getPersonID();
                            if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                //cimLotNote.setPersonID(cimToPerson);
                                cimLotNote.setPerson(cimToPerson);
                                updateFlag = true;
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimLotNoteDO
                            strUserDataSets = cimLotNote.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimLotNote.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
                //14.OWNER_CONFIRMED_AND_CHANGED_BY_PERSONID
                if (CimStringUtils.equals(currClass,BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE) && CimStringUtils.equals(currTable,"OMLOTOPEMEMO") && CimStringUtils.equals(currColumn,"OWNER_ID")){
                    if (bo instanceof com.fa.cim.newcore.bo.product.CimLotOperationNote){
                        com.fa.cim.newcore.bo.product.CimLotOperationNote cimLotOperationNote = (com.fa.cim.newcore.bo.product.CimLotOperationNote) bo;
                        if (!CimObjectUtils.isEmpty(cimLotOperationNote)){
                            String ownerID = cimLotOperationNote.getPersonID();
                            if (CimStringUtils.equals(ownerID,strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                //cimLotOperationNote.setPersonID(cimToPerson);
                                cimLotOperationNote.setPerson(cimToPerson);
                                updateFlag = true;
                            }
                        }else {
                            throw new ServiceException(retCodeConfig.getNotFoundObject());
                        }
                        for (int k = 0; k < CimArrayUtils.getSize(strOwnerChangeColumnDefinition.getStrUserDataSeq()); k++) {
                            Infos.UserData strUserData = strOwnerChangeColumnDefinition.getStrUserDataSeq().get(k);
                            Boolean existFlag = false;
                            String currName = strUserData.getName();
                            String currOrig = strUserData.getOriginator();
                            List<GlobalDTO.UserDataSet> strUserDataSets = new ArrayList<>();
                            GlobalDTO.UserDataSet strUserDataSet = new GlobalDTO.UserDataSet();

                            //getUserDataSetNamedAndOrig cimLotOpeNoteDO
                            strUserDataSets = cimLotOperationNote.getUserDataSetNamedAndOrig(currName, currOrig);
                            for (int m = 0; m < CimArrayUtils.getSize(strUserDataSets); m++) {
                                if (CimStringUtils.equals(strUserDataSets.get(m).getValue(),strOwnerChangeReqInParm.getFromOwnerID().getValue())){
                                    strUserDataSet = strUserDataSets.get(m);
                                    strUserDataSet.setValue(strOwnerChangeReqInParm.getToOwnerID().getValue());
                                    existFlag = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(existFlag)){
                                cimLotOperationNote.setUserDataSetNamedAndOrig(strUserDataSets);
                                updateFlag = true;
                            }
                        }
                    }
                }
            }
        }
        return updateFlag;
    }


    @Override
    public List<Infos.OwnerChangeObject> ownerChangeDefObjUpdateDR(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeDefObjUpdateDRIn input) {
        //init
        List<Infos.OwnerChangeObject> out = new ArrayList<>();
        int extendLen = 500;
        int tChgLen = extendLen;
        int chgCount = 0;
        List<Infos.OwnerChangeObject> strOwnerChangeObjectSeq = new ArrayList<>();

        String HV_BUFFER = "";
        String HV_TMPBUFFER = "";
        //----------------------------------------------------
        // Setting the environment variable
        // OM_OWNER_CHG_REMOVE_DUPLICATE
        // 0: Default value --> Disable Owner Change delete duplicate records.
        // 1 --> Enable Owner Change delete duplicate records.
        //----------------------------------------------------
        int duplicateCount;
        Boolean deleteDuplicateRecordFlag = false;
        Long value = StandardProperties.OM_OWNER_CHG_REMOVE_DUPLICATE.getLongValue();
        if (1 == value){
            deleteDuplicateRecordFlag = true;
            log.info("deleteDuplicateRecordFlag = TRUE");
        }else {
            log.info("deleteDuplicateRecordFlag = FALSE");
        }
        //----------------------------------------------------
        // Select records
        //----------------------------------------------------
        HV_TMPBUFFER = String.format(" %s",input.getStrOwnerChangeDefObjDefinition().getKeys());
        HV_BUFFER += HV_TMPBUFFER;

        HV_TMPBUFFER = String.format(" FROM %s",input.getStrOwnerChangeDefObjDefinition().getTableName());
        HV_BUFFER += HV_TMPBUFFER;

        HV_TMPBUFFER = String.format(" WHERE %s",input.getStrOwnerChangeDefObjDefinition().getColumnName());
        HV_BUFFER += HV_TMPBUFFER;

        HV_TMPBUFFER = String.format(" = '%s'",input.getFromOwnerID());
        HV_BUFFER += HV_TMPBUFFER;

        HV_BUFFER += " WITH RS USE AND KEEP UPDATE LOCKS ";

        //-------------------------------------------
        // Judge and Convert SQL with Escape Sequence
        //-------------------------------------------
        Boolean  bConvertFlag = false;
        String originalSQL = HV_BUFFER;
        List<Object[]> sqlDataOut = cimJpaRepository.query(originalSQL);

        //todo : we don't not need implement the method confirmed by qinlin...
        return out;
    }
}
