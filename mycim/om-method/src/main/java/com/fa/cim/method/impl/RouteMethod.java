package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.BaseStaticMethod;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.processflow.CimPFDefinitionListDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationMainPDDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IRouteMethod;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification;
import com.fa.cim.newcore.bo.pd.ProcessDefinitionManager;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/19        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/10/19 11:21
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class RouteMethod implements IRouteMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private CimFrameWorkGlobals cimFrameWorkGlobals;
    
    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    @Qualifier("ProcessDefinitionManagerCore")
    private ProcessDefinitionManager processDefinitionManager;

    @Override
    public List<ObjectIdentifier> routeProductListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier routeID) {
        log.debug("the routeID: {}", routeID);
        List<ObjectIdentifier> productIDList = new ArrayList<>();
        List<CimProductSpecificationMainPDDO> productSpecificationToMainProcessDefinitionList = cimJpaRepository.query("select ID from OMPRODINFO_PRP where MAIN_PROCESS_ID = ?1", CimProductSpecificationMainPDDO.class, routeID.getValue());

        int size = CimArrayUtils.getSize(productSpecificationToMainProcessDefinitionList);
        for (int i = 0; i < size; i++) {
            CimProductSpecificationMainPDDO productSpecificationToMainProcessDefinition = productSpecificationToMainProcessDefinitionList.get(i);

            String id = productSpecificationToMainProcessDefinition.getId();
            //get productID By productGroupID
            CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("select PROD_ID from OMPRODINFO where ID = ?1", CimProductSpecificationDO.class, id);
            if (null != productSpecification) {
                productIDList.add(new ObjectIdentifier(productSpecification.getProductSpecID()));
            }
        }
        return productIDList;
    }

    @Override
    public ObjectIdentifier routeOperationOperationIDGet(Infos.ObjCommon objCommon, ObjectIdentifier routeID, String operationNumber) {
        CimProcessDefinition aMainPD = baseCoreFactory.getBO(CimProcessDefinition.class, routeID);
        Validations.check(aMainPD == null, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), routeID.getValue()));
        //【step1】getting active version of route
        log.debug("【step1】getting active version of route");
        String version = cimFrameWorkGlobals.extractVersionFromID(routeID.getValue());
        if (CimStringUtils.equals(BizConstant.SP_ACTIVE_VERSION, version)) {
            log.debug("route is version controlled");
            aMainPD = aMainPD.getActiveObject();
        }
        CimProcessFlow aMainPF = aMainPD.getActiveMainProcessFlow();
        Validations.check(null == aMainPF, new OmCode(retCodeConfig.getNotFoundProcessFlow(), ""));

        AtomicReference<CimProcessFlow> outMainProcessFlow = new AtomicReference<>();
        AtomicReference<CimProcessFlow> outModuleProcessFlow = new AtomicReference<>();
        CimProcessOperationSpecification modulePos = aMainPF.getProcessOperationSpecificationFor(operationNumber, outMainProcessFlow, outModuleProcessFlow);
        Validations.check(null == modulePos || null == outMainProcessFlow.get() || null == outModuleProcessFlow.get(),
                new OmCode(retCodeConfig.getNotFoundRouteOpe(), ObjectIdentifier.fetchValue(routeID), operationNumber));

        //【step2】check whether specified product is base product.
        log.debug("【step2】check whether specified product is base product.");
        List<ProcessDefinition> aPDs = modulePos.getProcessDefinitions();
        Validations.check(CimArrayUtils.isEmpty(aPDs) || CimArrayUtils.getSize(aPDs) != 1,
                new OmCode(retCodeConfig.getNotFoundRouteOpe(), ObjectIdentifier.fetchValue(routeID), operationNumber));
        CimProcessDefinition aPD = (CimProcessDefinition) aPDs.get(0);
        Validations.check(null == aPD, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
        return new ObjectIdentifier(aPD.getIdentifier(), aPD.getPrimaryKey());
    }

    @Override
    public List<Infos.BOMPartsDefInProcess> routeBOMPartsDefinitionGetDR(Infos.ObjCommon objCommon, ObjectIdentifier bomID, ObjectIdentifier routeID) {
        Validations.check(null == bomID || null == routeID, retCodeConfig.getInvalidParameter());

        List<Infos.BOMPartsDefInProcess> retVal = new ArrayList<>();

        CimProcessDefinition mainPD = processDefinitionManager.findMainProcessDefinitionNamed(ObjectIdentifier.fetchValue(routeID));
        Validations.check(null == mainPD, retCodeConfig.getNotFoundMainRoute());
        CimProcessFlow activeMainPF = mainPD.getActiveMainProcessFlow();
        Validations.check(null == activeMainPF, retCodeConfig.getNotFoundMainRoute());

        //---------------------------------------------------------------------------------
        //   Get BOM Information.
        //---------------------------------------------------------------------------------
        String sql  = "select    PDLIST.ROUTE_NO,\n" +
                "                POS.OPE_NO,\n" +
                "                CASE " +
                "                    WHEN OPEPD.ACTIVE_VER_ID IS NULL\n" +
                "                    THEN OPEPD.ID\n" +
                "                    ELSE OPEPD.ACTIVE_VER_ID\n" +
                "                END AS ID,\n" +
                "                CASE " +
                "                    WHEN OPEPD.ACTIVE_VER_ID IS NULL\n" +
                "                    THEN OPEPD.PRP_ID\n" +
                "                    ELSE OPEPD.ACTIVE_VER_RKEY\n" +
                "                END AS REFKEY,\n" +
                "                BPARTS.PARTS_ID,       BPARTS.PARTS_RKEY,\n" +
                "                BPARTS.QTY \n" +
                "        from       OMPRF_ROUTESEQ  PDLIST\n" +
                "        inner join OMPRP         MODPD   on  MODPD.PRP_ID            = PDLIST.ROUTE_ID\n" +
                "                                        and MODPD.PRP_LEVEL         = 'Module'\n" +
                "        left  join OMPRP         MODPDV  on  MODPDV.PRP_ID           = MODPD.ACTIVE_VER_ID\n" +
                "                                        and MODPDV.PRP_LEVEL        = MODPD.PRP_LEVEL\n" +
                "        inner join OMPRF         MODPF   on  MODPF.ID               = coalesce( MODPDV.ACTIVE_PRF_RKEY, MODPD.ACTIVE_PRF_RKEY )\n" +
                "        inner join OMPRF_PRSSSEQ POSLIST on  POSLIST.REFKEY         = MODPF.ID\n" +
                "        inner join OMPRSS        POS     on  POS.ID                 = POSLIST.PRSS_RKEY\n" +
                "        inner join OMPRP         OPEPD   on  OPEPD.PRP_ID            = POS.STEP_ID\n" +
                "                                        and OPEPD.PRP_LEVEL         = 'Operation'\n" +
                "        inner join OMPRP_PARTS   PPARTS  on  PPARTS.REFKEY          = OPEPD.ID\n" +
                "        inner join OMBOM_PARTS  BPARTS  on  BPARTS.PARTS_ID        = PPARTS.PARTS_ID\n" +
                "        inner join OMBOM        BOM     on  BOM.ID                 = BPARTS.REFKEY\n" +
                "        where   PDLIST.REFKEY   = ?1 \n" +
                "        and     BOM.BOM_ID      = ?2 \n" +
                "        order by PDLIST.IDX_NO, POSLIST.IDX_NO";
        log.info("SQL: " + sql);
        List<Object[]> query = cimJpaRepository.query(sql, activeMainPF.getPrimaryKey(), bomID.getValue());
        Optional.ofNullable(query).ifPresent(out -> out.forEach(data -> {
            String pfPDListModuleNo = String.valueOf(data[0]);
            String posOpeNo = String.valueOf(data[1]);
            String posPDObj = String.valueOf(data[2]);
            String posPDId = String.valueOf(data[3]);
            String bomPartsPartsId = String.valueOf(data[4]);
            String bomPartsPartsObj = String.valueOf(data[5]);
            int bomPartsQuantity = Integer.parseInt(String.valueOf(data[6]));

            String aOpeNo = BaseStaticMethod.convertModuleOpeNoToOpeNo(pfPDListModuleNo, posOpeNo);
            boolean foundFlag = false;
            int index = 0;
            int tmpLoop = 0;
            for (Infos.BOMPartsDefInProcess partsDefInProcess : retVal) {
                if (CimStringUtils.equals(partsDefInProcess.getOperationNumber(), aOpeNo)) {
                    foundFlag = true;
                    index = tmpLoop;
                    break;
                }
                tmpLoop++;
            }
            if (foundFlag) {
                Infos.BOMPartsInfo bom = new Infos.BOMPartsInfo();
                bom.setPartID(ObjectIdentifier.build(bomPartsPartsId, bomPartsPartsObj));
                bom.setQty(bomPartsQuantity);
                retVal.get(index).getStrBOMPartsInfoSeq().add(bom);
            } else {
                Infos.BOMPartsDefInProcess bomPartsDefInProcess = new Infos.BOMPartsDefInProcess();
                bomPartsDefInProcess.setRouteID(routeID);
                bomPartsDefInProcess.setOperationNumber(aOpeNo);
                bomPartsDefInProcess.setOperationID(ObjectIdentifier.build(posPDId, posPDObj));

                List<Infos.BOMPartsInfo> bomPartsInfos = new ArrayList<>();
                Infos.BOMPartsInfo bom = new Infos.BOMPartsInfo();
                bom.setPartID(ObjectIdentifier.build(bomPartsPartsId, bomPartsPartsObj));
                bom.setQty(bomPartsQuantity);
                bomPartsInfos.add(bom);
                bomPartsDefInProcess.setStrBOMPartsInfoSeq(bomPartsInfos);

                retVal.add(bomPartsDefInProcess);
            }
        }));
        return retVal;
    }

    @Override
    public List<Infos.OperationInfo> routeConnectedSubRouteGetDR(Infos.ObjCommon objCommon, ObjectIdentifier mainRouteID) {
        Validations.check(null == objCommon || null == mainRouteID, retCodeConfig.getInvalidInputParam());
        //----------------------------------------------------------------------
        //   Getting Process Flow
        //----------------------------------------------------------------------
        CimProcessDefinition mainPD = processDefinitionManager.findMainProcessDefinitionNamed(ObjectIdentifier.fetchValue(mainRouteID));
        Validations.check(null == mainPD, retCodeConfig.getNotFoundRoute());

        CimProcessFlow activeMainPF = mainPD.getActiveMainProcessFlow();
        Validations.check(null == activeMainPF, retCodeConfig.getNotFoundRoute());
        CimProcessFlow activePF = mainPD.getActiveProcessFlow();
        Validations.check(null == activePF, retCodeConfig.getNotFoundRoute());

        String hFRPDACTIVE_MAINPF_OBJ = activeMainPF.getPrimaryKey();
        //---------------------------------------------------------------------------------
        //   Get Main PD Connected Sub Route Information.
        //---------------------------------------------------------------------------------
        String hFRPFd_theSystemKey = activePF.getPrimaryKey();
        String sql = "SELECT\n" +
                "       POSLIST.IDX_NO,\n" +
                "       POS.OPE_NO,\n" +
                "       CASE\n" +
                "           WHEN SUBPD.ACTIVE_VER_ID IS NULL\n" +
                "           THEN SUBPD.PRP_ID\n" +
                "           ELSE SUBPD.ACTIVE_VER_ID\n" +
                "       END AS ID,\n" +
                "       CASE\n" +
                "           WHEN SUBPD.ACTIVE_VER_ID IS NULL\n" +
                "           THEN SUBPD.ID\n" +
                "           ELSE SUBPD.ACTIVE_VER_RKEY\n" +
                "       END AS REFKEY,\n" +
                "       SUBPD.PRP_TYPE,\n" +
                "       SUBPF.RTN_OPE_NO\n" +
                "from       OMPRF_PRSSSEQ POSLIST\n" +
                "inner join OMPRSS        POS     on  POS.ID               = POSLIST.PRSS_RKEY\n" +
                "inner join OMPRSS_SUBPRF  SUBPF   on  SUBPF.REFKEY         = POS.ID\n" +
                "inner join OMPRP         SUBPD   on  SUBPD.PRP_ID          = SUBPF.SUB_PROCESS_ID\n" +
                "                                and SUBPD.PRP_LEVEL       = ?1 \n" +
                "where   POSLIST.REFKEY = ?2 \n" +
                "order by POSLIST.IDX_NO";
        List<Object[]> results = cimJpaRepository.query(sql, BizConstant.SP_PD_FLOWLEVEL_MAIN, hFRPFd_theSystemKey);

        hFRPFd_theSystemKey = hFRPDACTIVE_MAINPF_OBJ;
        List<Infos.OperationInfo> strMainOperationInfoSeq = new ArrayList<>();
        List<Integer> mainModuleSeqNoSeq = new ArrayList<>();
        List<Integer> mainOperationSeqNoSeq = new ArrayList<>();

        int nOpeCnt = 0;
        int prevPosSeqNo = 0;
        if (CimArrayUtils.isNotEmpty(results)) {
            for (Object[] object : results) {
                int pfPOSListSeqNo = Integer.parseInt(String.valueOf(object[0]));
                String opeNo = String.valueOf(object[1]);
                String subPFBranchMainPDObj = String.valueOf(object[3]);
                String subPFBranchMainPDId = String.valueOf(object[2]);
                String pdType = String.valueOf(object[4]);
                String subPFReturnOpeNo = String.valueOf(object[5]);

                Infos.OperationInfo operationInfo;
                if (nOpeCnt == 0 || prevPosSeqNo != pfPOSListSeqNo) {
                    operationInfo = new Infos.OperationInfo();
                    prevPosSeqNo = pfPOSListSeqNo;
                    //------------------------------------------------------
                    //   Convert moduleNo and moduleOpeNo from OpeNo.
                    //------------------------------------------------------

                    String moduleNo = BaseStaticMethod.convertOpeNoToModuleNo(opeNo);
                    String moduleOpeNo = BaseStaticMethod.convertOpeNoToModuleOpeNo(opeNo);

                    CimPFDefinitionListDO example = new CimPFDefinitionListDO();
                    example.setModuleNO(moduleNo);
                    example.setReferenceKey(hFRPFd_theSystemKey);

                    CimPFDefinitionListDO pfDefinitionListDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);
                    Validations.check(null == pfDefinitionListDO, retCodeConfig.getNotFoundModuleNo());

                    String hFRPF_PDLISTPD_ID = pfDefinitionListDO.getProcessDefinitionID();

                    String version = BaseStaticMethod.extractVersionFromID(pfDefinitionListDO.getProcessDefinitionID());
                    if (CimStringUtils.equals(version, BizConstant.SP_ACTIVE_VERSION)) {
                        log.info("Module is Version Controlled");
                        CimProcessDefinitionDO example1 = new CimProcessDefinitionDO();
                        example1.setProcessDefinitionID(pfDefinitionListDO.getProcessDefinitionID());
                        example1.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_MODULE);
                        CimProcessDefinitionDO modulePD = cimJpaRepository.findOne(Example.of(example1)).orElse(null);
                        Validations.check(null == modulePD || CimStringUtils.isEmpty(modulePD.getActiveID()), retCodeConfig.getNotFoundModule());
                        hFRPF_PDLISTPD_ID = modulePD.getActiveID();
                    }

                    sql = "select  POSLIST.IDX_NO,\n" +
                            "      POS.STEP_ID,           \n" +
                            "      POS.STEP_RKEY,\n" +
                            "      POS.COMPULSORY_FLAG\n" +
                            "from       OMPRF         MODPF\n" +
                            "inner join OMPRF_PRSSSEQ POSLIST on  POSLIST.REFKEY    = MODPF.ID\n" +
                            "                                and POSLIST.LINK_KEY     = ?1\n" +
                            "inner join OMPRSS        POS     on  POS.ID            = POSLIST.PRSS_RKEY\n" +
                            "where   MODPF.PRP_ID = ?2\n" +
                            "and     MODPF.PRP_LEVEL  = ?3\n" +
                            "and     MODPF.ACTIVE_FLAG     = 1";
                    Object[] oneResult = cimJpaRepository.queryOne(sql, moduleOpeNo, hFRPF_PDLISTPD_ID, BizConstant.SP_PD_FLOWLEVEL_MODULE);
                    Validations.check(null == oneResult, retCodeConfig.getNotFoundOperation());
                    operationInfo.setOperationID(ObjectIdentifier.build(String.valueOf(oneResult[1]), String.valueOf(oneResult[2])));
                    operationInfo.setOperationNumber(opeNo);
                    operationInfo.setMandatoryFlag(CimStringUtils.equals(String.valueOf(oneResult[3]), "1"));

                    mainModuleSeqNoSeq.add(pfDefinitionListDO.getSequenceNumber());
                    mainOperationSeqNoSeq.add(Integer.parseInt(String.valueOf(oneResult[0])));
                    strMainOperationInfoSeq.add(operationInfo);
                    nOpeCnt++;
                }

                operationInfo = strMainOperationInfoSeq.get(nOpeCnt - 1);
                List<Infos.ConnectedRoute> connectedRouteList = operationInfo.getConnectedRouteList();
                if (CimArrayUtils.isEmpty(connectedRouteList)) {
                    connectedRouteList = new ArrayList<>();
                }
                Infos.ConnectedRoute connectedRoute = new Infos.ConnectedRoute();
                connectedRoute.setRouteID(ObjectIdentifier.build(subPFBranchMainPDId, subPFBranchMainPDObj));
                connectedRoute.setProcessDefinitionType(pdType);
                connectedRoute.setReturnOperationNumber(subPFReturnOpeNo);
                connectedRouteList.add(connectedRoute);
                operationInfo.setConnectedRouteList(connectedRouteList);
            }
        }

        //---------------------------------------------------------------------------------
        //   Get Module PD Connected Sub Route Information.
        //---------------------------------------------------------------------------------
        sql = "SELECT\n" +
                "    PDLIST.IDX_NO,\n" +
                "    PDLIST.ROUTE_NO,\n" +
                "    POSLIST.IDX_NO as POSLIST_SeqNo,\n" +
                "    POS.OPE_NO,\n" +
                "    POS.STEP_ID,\n" +
                "    POS.STEP_RKEY,\n" +
                "    POS.COMPULSORY_FLAG,\n" +
                "    CASE\n" +
                "        WHEN SUBPD.ACTIVE_VER_ID IS NULL\n" +
                "        THEN SUBPD.PRP_ID\n" +
                "        ELSE SUBPD.ACTIVE_VER_ID\n" +
                "    END AS ID,\n" +
                "    CASE\n" +
                "        WHEN SUBPD.ACTIVE_VER_ID IS NULL\n" +
                "        THEN SUBPD.ID\n" +
                "        ELSE SUBPD.ACTIVE_VER_RKEY\n" +
                "    END AS REFKEY,\n" +
                "    SUBPD.PRP_TYPE,\n" +
                "    SUBPF.RTN_OPE_NO\n" +
                "from       OMPRF_ROUTESEQ  PDLIST\n" +
                "inner join OMPRP         MODPD   on  MODPD.PRP_ID            = PDLIST.ROUTE_ID\n" +
                "                                and MODPD.PRP_LEVEL         = ?1\n" +
                "inner join OMPRF         MODPF   on  MODPF.PRP_ID        = CASE \n" +
                "                                                                 WHEN MODPD.ACTIVE_VER_ID IS NULL\n" +
                "                                                                 THEN PDLIST.ROUTE_ID\n" +
                "                                                                 ELSE MODPD.ACTIVE_VER_ID\n" +
                "                                                             END\n" +
                "                                and MODPF.PRP_LEVEL         = MODPD.PRP_LEVEL\n" +
                "                                and MODPF.ACTIVE_FLAG            = 1\n" +
                "inner join OMPRF_PRSSSEQ POSLIST on  POSLIST.REFKEY         = MODPF.ID\n" +
                "inner join OMPRSS        POS     on  POS.ID                 = POSLIST.PRSS_RKEY\n" +
                "inner join OMPRSS_SUBPRF  SUBPF   on  SUBPF.REFKEY           = POS.ID\n" +
                "inner join OMPRP         SUBPD   on  SUBPD.PRP_ID            = SUBPF.SUB_PROCESS_ID\n" +
                "                                and SUBPD.PRP_LEVEL         = ?2\n" +
                "where   PDLIST.REFKEY = ?3\n" +
                "order by PDLIST.IDX_NO, POSLIST.IDX_NO";
        results = cimJpaRepository.query(sql, BizConstant.SP_PD_FLOWLEVEL_MODULE, BizConstant.SP_PD_FLOWLEVEL_MAIN, hFRPFd_theSystemKey);
        List<Infos.OperationInfo> strModOperationInfoSeq = new ArrayList<>();
        List<Integer> modModuleSeqNoSeq = new ArrayList<>();
        List<Integer> modOperationSeqNoSeq = new ArrayList<>();

        int prevPdSeqNo = 0;
        if (CimArrayUtils.isNotEmpty(results)) {
            nOpeCnt = 0;
            for (Object[] object : results) {
                int pfPDListSeqNo = Integer.parseInt(String.valueOf(object[0]));
                String pfPDListModuleNo = String.valueOf(object[1]);
                int pfPOSListSeqNo = Integer.parseInt(String.valueOf(object[2]));
                String posOpeNo = String.valueOf(object[3]);
                String posPDId = String.valueOf(object[4]);
                String posPDObj = String.valueOf(object[5]);
                String posMandatoryFlag = String.valueOf(object[6]);
                String posSubPFBranchMainPDId = String.valueOf(object[7]);
                String posSubPFBranchMainPDObj = String.valueOf(object[8]);
                String pdType = String.valueOf(object[9]);
                String posSubPFReturnOpeNo = String.valueOf(object[10]);

                Infos.OperationInfo operationInfo;
                if (0 == nOpeCnt || prevPdSeqNo != pfPDListSeqNo || prevPosSeqNo != pfPOSListSeqNo) {
                    operationInfo = new Infos.OperationInfo();
                    prevPdSeqNo = pfPDListSeqNo;
                    prevPosSeqNo = pfPOSListSeqNo;

                    operationInfo.setOperationID(ObjectIdentifier.build(posPDId, posPDObj));
                    operationInfo.setOperationNumber(BaseStaticMethod.convertModuleOpeNoToOpeNo(pfPDListModuleNo, posOpeNo));
                    operationInfo.setMandatoryFlag(CimStringUtils.equals(posMandatoryFlag, "1"));

                    strModOperationInfoSeq.add(operationInfo);
                    modModuleSeqNoSeq.add(pfPDListSeqNo);
                    modOperationSeqNoSeq.add(pfPOSListSeqNo);

                    nOpeCnt++;
                }

                operationInfo = strModOperationInfoSeq.get(nOpeCnt - 1);
                List<Infos.ConnectedRoute> connectedRouteList = operationInfo.getConnectedRouteList();
                if (null == connectedRouteList) {
                    connectedRouteList = new ArrayList<>();
                }
                Infos.ConnectedRoute connectedRoute = new Infos.ConnectedRoute();
                connectedRoute.setRouteID(ObjectIdentifier.build(posSubPFBranchMainPDId, posSubPFBranchMainPDObj));
                connectedRoute.setProcessDefinitionType(pdType);
                connectedRoute.setReturnOperationNumber(posSubPFReturnOpeNo);
                connectedRouteList.add(connectedRoute);
                operationInfo.setConnectedRouteList(connectedRouteList);
            }
        }

        int nMainSeqLen = CimArrayUtils.getSize(strMainOperationInfoSeq);
        int nModSeqLen = CimArrayUtils.getSize(strModOperationInfoSeq);
        int nMainSeqNo = 0;
        int nModSeqNo = 0;

        List<Infos.OperationInfo> strOperationInfoSeq = new ArrayList<>();
        while (nMainSeqNo < nMainSeqLen && nModSeqNo < nModSeqLen) {
            if (mainModuleSeqNoSeq.get(nMainSeqNo) < modModuleSeqNoSeq.get(nModSeqNo)
                    || Objects.equals(mainModuleSeqNoSeq.get(nMainSeqNo), modModuleSeqNoSeq.get(nModSeqNo))
                    && mainOperationSeqNoSeq.get(nMainSeqNo) < modOperationSeqNoSeq.get(nModSeqNo)) {
                log.info("Add MainPD ConnectedRoute");
                strOperationInfoSeq.add(strMainOperationInfoSeq.get(nMainSeqNo++));
            } else if (mainModuleSeqNoSeq.get(nMainSeqNo) > modModuleSeqNoSeq.get(nModSeqNo)
                    || Objects.equals(mainModuleSeqNoSeq.get(nMainSeqNo), modModuleSeqNoSeq.get(nModSeqNo))
                    && mainOperationSeqNoSeq.get(nMainSeqNo) > modOperationSeqNoSeq.get(nModSeqNo)) {
                log.info("Add ModPD  ConnectedRoute");
                strOperationInfoSeq.add(strModOperationInfoSeq.get(nModSeqNo++));
            } else {
                log.info("Concatenate MainPD and ModPD ConnectedRoute");
                strOperationInfoSeq.add(strMainOperationInfoSeq.get(nMainSeqNo));
                List<Infos.ConnectedRoute> connectedRouteListForMain = strMainOperationInfoSeq.get(nMainSeqNo).getConnectedRouteList();
                List<Infos.ConnectedRoute> connectedRouteListForModule = strModOperationInfoSeq.get(nModSeqNo).getConnectedRouteList();
                connectedRouteListForMain.addAll(connectedRouteListForModule);
                nMainSeqNo++;
                nModSeqNo++;
            }
        }
        while (nMainSeqNo < nMainSeqLen) {
            log.info("Add MainPD ConnectedRoute");
            strOperationInfoSeq.add(strMainOperationInfoSeq.get(nMainSeqNo++));
        }

        while (nModSeqNo < nModSeqLen) {
            log.info("Add ModPD  ConnectedRoute");
            strOperationInfoSeq.add(strModOperationInfoSeq.get(nModSeqNo++));
        }

        log.info("strOperationInfoSeq length : " + CimArrayUtils.getSize(strOperationInfoSeq));
        for (Infos.OperationInfo operationInfo : strOperationInfoSeq) {
            //----------------------------------------------------------------------
            //   Getting Active Version of Route
            //----------------------------------------------------------------------
            String operationID = operationInfo.getOperationID().getValue();
            String version = BaseStaticMethod.extractVersionFromID(operationID);
            log.info("version : " + version);
            if (CimStringUtils.equals(version, BizConstant.SP_ACTIVE_VERSION)) {
                log.info("Operation is Version Controlled");
                CimProcessDefinitionDO example = new CimProcessDefinitionDO();
                example.setProcessDefinitionID(operationID);
                example.setProcessDefinitionLevel(BizConstant.SP_PD_FLOWLEVEL_OPERATION);
                CimProcessDefinitionDO processDefinitionDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);
                Validations.check(null == processDefinitionDO || CimStringUtils.isEmpty(processDefinitionDO.getActiveID()), retCodeConfig.getNotFoundOperation());
                operationInfo.setOperationID(ObjectIdentifier.build(processDefinitionDO.getActiveID(), processDefinitionDO.getActiveObj()));
            }
        }
        return strOperationInfoSeq;
    }
}