package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.IControlJobMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.method.IRecipeMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * description:
 * RecipeCompImpl .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/30        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/7/30 17:19
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class RecipeMethod  implements IRecipeMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IRecipeMethod recipeMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public void recipeParameterCheckConditionForOpeStart(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList, boolean processJobPauseFlag) {
        /*
         author:PlayBoy
         */
        log.debug("recipeParameterCheckConditionForOpeStart(): enter recipeParameterCheckConditionForOpeStart");
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), retCodeConfig.getNotFoundEquipment());
        //Validations.check(StringUtils.isEmpty(portGroupID), "portGroupID can not be empty", objCommon.getTransactionID());
        if (!processJobPauseFlag) {
            return;
        }
        //Check MultiRecipeCapability of EQP
        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        String multiRecipeCapability = equipment.getMultipleRecipeCapability();
        Validations.check(!CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE, multiRecipeCapability),
                retCodeConfig.getCassetteEquipmentConditionError(), objCommon.getTransactionID());
        //Check OperationMode of PortGroup of EQP
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);

        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        Predicate<Infos.EqpPortStatus> portGroupFilter = eqpPortStatus -> CimStringUtils.equals(portGroupID, eqpPortStatus.getPortGroup());
        eqpPortStatuses.stream()
                .filter(portGroupFilter)
                .forEach(eqpPortStatus -> {
                    String onlineMode = eqpPortStatus.getOnlineMode();
                    String operationStartMode = eqpPortStatus.getMoveInMode();
                    boolean isGoodCondition = CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE, onlineMode)
                            && CimStringUtils.equals(BizConstant.SP_EQP_STARTMODE_MANUAL, operationStartMode);
                    Validations.check(!isGoodCondition, retCodeConfig.getInvalidFromEqpMode(), objCommon.getTransactionID());
                });
        //Check Count of processJobExecFlag of StartCassetteInfo
        AtomicBoolean execFlag = new AtomicBoolean(false);
        Predicate<Infos.LotInCassette> lotInCassettePredicate = lotInCassette -> lotInCassette.getMoveInFlag();
        startCassetteList.forEach(startCassette -> {
            for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                if (lotInCassettePredicate.test(lotInCassette)) {
                    Validations.check(!CimStringUtils.equals(BizConstant.SP_RPARM_CHANGETYPE_BYWAFER, lotInCassette.getRecipeParameterChangeType()),
                            retCodeConfig.getInvalidInputParam(), objCommon.getTransactionID());
                    for (Infos.LotWafer lotWafer : lotInCassette.getLotWaferList()) {
                        if (lotWafer.getProcessJobExecFlag()) {
                            execFlag.set(true);
                            break;
                        }
                    }
                    if (execFlag.get()) {
                        break;
                    }
                }
            }
        });
        Validations.check(!execFlag.get(), retCodeConfig.getInvalidInputParam(), objCommon.getTransactionID());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strRecipeParameterAdjustHistoryGetDRIn
     * @return com.fa.cim.common.support.RetCode<com.fa.cim.dto.Infos.RecipeParameterAdjustHistoryGetDROut>
     * @exception
     * @author Ho
     * @date 2019/4/29 16:49
     */
    @Override
    public Outputs.RecipeParameterAdjustHistoryGetDROut recipeParameterAdjustHistoryGetDR(Infos.ObjCommon strObjCommonIn, Infos.RecipeParameterAdjustHistoryGetDRIn strRecipeParameterAdjustHistoryGetDRIn) {
        Validations.check(!CimStringUtils.equals(strRecipeParameterAdjustHistoryGetDRIn.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_RECIPEPARAMETERADJUST),
                new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "History Category is not correct"));

        Validations.check(1 != CimArrayUtils.getSize(strRecipeParameterAdjustHistoryGetDRIn.getStrTargetTableInfoSeq()),
                new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "Invalid table is requested"));

        Infos.TargetTableInfo strTargetTableInfo = strRecipeParameterAdjustHistoryGetDRIn.getStrTargetTableInfoSeq().get(0);
        Validations.check(3 != CimArrayUtils.getSize(strTargetTableInfo.getStrHashedInfoSeq()), new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "Invalid HashedInfoSeq length"));

        String controlJobID = "";
        String processJobID = "";
        String claimTime = "";
        SearchCondition searchCondition = strRecipeParameterAdjustHistoryGetDRIn.getSearchCondition();

        for (int iCnt1 = 0; iCnt1 < CimArrayUtils.getSize(strTargetTableInfo.getStrHashedInfoSeq()); iCnt1++) {
            if (CimStringUtils.equals(strTargetTableInfo.getStrHashedInfoSeq().get(iCnt1).getHashKey(),
                    BizConstant.SP_HISTORYCOLUMNNAME_CONTROLJOBID)) {
                controlJobID = strTargetTableInfo.getStrHashedInfoSeq().get(iCnt1).getHashData();
            } else if (CimStringUtils.equals(strTargetTableInfo.getStrHashedInfoSeq().get(iCnt1).getHashKey(),
                    BizConstant.SP_HISTORYCOLUMNNAME_PROCESSJOBID)) {
                processJobID = strTargetTableInfo.getStrHashedInfoSeq().get(iCnt1).getHashData();
            } else if (CimStringUtils.equals(strTargetTableInfo.getStrHashedInfoSeq().get(iCnt1).getHashKey(),
                    BizConstant.SP_HISTORYCOLUMNNAME_CLAIMEDTIME)) {
                claimTime = strTargetTableInfo.getStrHashedInfoSeq().get(iCnt1).getHashData();
            }
        }

        Validations.check(CimObjectUtils.isEmpty(controlJobID), new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "ControlJob ID is mandatory"));
        Validations.check(CimObjectUtils.isEmpty(claimTime) || CimStringUtils.equals(claimTime, BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING), new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "claimTime is mandatory"));

        if (0 == CimStringUtils.length(processJobID)) {
            processJobID = "%";
        }

        Outputs.RecipeParameterAdjustHistoryGetDROut strRecipeParameterAdjustHistoryGetDROut = new Outputs.RecipeParameterAdjustHistoryGetDROut();
        List<Infos.TableRecordInfo> strTableRecordInfoSeq = new ArrayList<>();
        strRecipeParameterAdjustHistoryGetDROut.setStrTableRecordInfoSeq(strTableRecordInfoSeq);
        Infos.TableRecordInfo strTableRecordInfo = new Infos.TableRecordInfo();
        strTableRecordInfoSeq.add(strTableRecordInfo);
        strTableRecordInfo.setTableName(BizConstant.SP_HISTORYTABLENAME_FHPJCHS_RPARM);
        List<String> columnNames = new ArrayList<>();
        strTableRecordInfo.setColumnNames(columnNames);
        int columnCount = 0;
        columnNames.add(BizConstant.SP_HISTORYCOLUMNNAME_RECIPEPARAMETERNAME);
        columnCount++;
        columnNames.add(BizConstant.SP_HISTORYCOLUMNNAME_PREVIOUSVALUE);
        columnCount++;
        columnNames.add(BizConstant.SP_HISTORYCOLUMNNAME_CHANGEDVALUE);
        columnCount++;

        String hFHPJCHSCTRLJOB_ID = controlJobID;
        String hFHPJCHSPRCSJOB_ID = processJobID;
        String hFHPJCHSCLAIM_TIME = claimTime;

        String sql = "SELECT RPARAM_NAME,\n" +
                "                PREV_RPARAM_VAL,\n" +
                "                RPARAM_VAL\n" +
                "        FROM   OHPJCHG, OHPJCHG_RPARAM\n" +
                "        WHERE  OHPJCHG.CJ_ID = ?\n" +
                "        AND    OHPJCHG.PJ_ID = OHPJCHG_RPARAM.PJ_ID\n" +
                "        AND    OHPJCHG.TRX_TIME = OHPJCHG_RPARAM.TRX_TIME\n" +
                "        AND    OHPJCHG_RPARAM.PJ_ID LIKE ?\n" +
                "        AND    OHPJCHG_RPARAM.TRX_TIME = to_timestamp(?, 'yyyy-mm-dd hh24:mi:ss.ff')\n" +
                "        ORDER BY OHPJCHG_RPARAM.TRX_TIME, OHPJCHG_RPARAM.PJ_ID";
        Page<Object[]> queryPage =null;
        List<Object[]> RCPC = null;
        if (searchCondition!=null){
            queryPage = cimJpaRepository.query(sql, searchCondition, hFHPJCHSCTRLJOB_ID, hFHPJCHSPRCSJOB_ID, hFHPJCHSCLAIM_TIME);
            RCPC = queryPage.getContent();
        }else {
            RCPC = cimJpaRepository.query(sql, hFHPJCHSCTRLJOB_ID, hFHPJCHSPRCSJOB_ID, hFHPJCHSCLAIM_TIME);
        }


        int count = 0;
        int increasedDataLen = 10;
        List<Infos.TableRecordValue> strTableRecordValueSeq = new ArrayList<>();
        strRecipeParameterAdjustHistoryGetDROut.setStrTableRecordValueSeq(strTableRecordValueSeq);

        if (RCPC!=null)
        for (Object[] obj : RCPC) {
            String hFHPJCHS_RPARMRPARM_NAME = "";
            String hFHPJCHS_RPARMPRE_RPARM_VALUE = "";
            String hFHPJCHS_RPARMRPARM_VALUE = "";

            hFHPJCHS_RPARMRPARM_NAME = CimObjectUtils.toString(obj[0]);
            hFHPJCHS_RPARMPRE_RPARM_VALUE = CimObjectUtils.toString(obj[1]);
            hFHPJCHS_RPARMRPARM_VALUE = CimObjectUtils.toString(obj[2]);

            if (count >= increasedDataLen) {
                increasedDataLen += 10;
            }

            Infos.TableRecordValue strTableRecordValue = new Infos.TableRecordValue();
            strTableRecordValueSeq.add(strTableRecordValue);
            strTableRecordValue.setTableName(BizConstant.SP_HISTORYTABLENAME_FHPJCHS_RPARM);
            strTableRecordValue.setReportTimeStamp(hFHPJCHSCLAIM_TIME);

            List<Object> columnValues = new ArrayList<>();
            strTableRecordValue.setColumnValues(columnValues);
            columnCount = 0;
            columnValues.add(hFHPJCHS_RPARMRPARM_NAME);
            columnCount++;
            columnValues.add(hFHPJCHS_RPARMPRE_RPARM_VALUE);
            columnCount++;
            columnValues.add(hFHPJCHS_RPARMRPARM_VALUE);
            columnCount++;
            count++;
        }
        if (searchCondition!=null){
            strRecipeParameterAdjustHistoryGetDROut.setStrTableRecordValuePage(CimPageUtils.convertListToPage(strTableRecordValueSeq,searchCondition.getPage(),searchCondition.getSize(),queryPage.getTotalElements()));
        }
        return strRecipeParameterAdjustHistoryGetDROut;
    }

    public List<ObjectIdentifier> recipeRecipeIDGetDR(Infos.ObjCommon objCommon) {
        log.info("【Method Entry】recipeRecipeIDGetDR()");
        List<ObjectIdentifier> recipeIDs = new ArrayList<>();

        String versionID = BizConstant.SP_ACTIVE_VERSION;
        List<CimMachineRecipeDO> MRecipeList = cimJpaRepository.query("SELECT RECIPE_ID, ID FROM OMRCP WHERE VERSION_ID != ?1", CimMachineRecipeDO.class, versionID);

        int count = CimArrayUtils.getSize(MRecipeList);
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                CimMachineRecipeDO machineRecipe = MRecipeList.get(i);
                recipeIDs.add(new ObjectIdentifier(machineRecipe.getRecipeID(), machineRecipe.getId()));
            }
        }

        log.info("【Method Exit】recipeRecipeIDGetDR()");
        return recipeIDs;
    }

    @Override
    public  List<Infos.StartCassette> recipeParameterAdjustConditionCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette, boolean allProcessStartFlag) {
        int searchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getIntValue();
        /*----------------------------------------*/
        /*   Check MultiRecipeCapability of EQP   */
        /*----------------------------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
        boolean bProcessJobLevelCtrl = aMachine.isProcessJobLevelControlOn();
        Validations.check(bProcessJobLevelCtrl, new OmCode(retCodeConfig.getPjctrlAvailable(), equipmentID.getValue()));
        String multiRecipeCapability = aMachine.getMultipleRecipeCapability();
        Validations.check(!CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE), retCodeConfig.getCassetteEquipmentConditionError());
        /*----------------------*/
        /*   Check ControlJob   */
        /*----------------------*/
        CimControlJob aControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(aControlJob == null, retCodeConfig.getNotFoundControlJob());
        CimMachine aReserveMachine = aControlJob.getMachine();
        ObjectIdentifier resvEqpID = new ObjectIdentifier(aReserveMachine.getIdentifier(), aReserveMachine.getPrimaryKey());
        if (!ObjectIdentifier.equalsWithValue(equipmentID, resvEqpID)){
            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundEqpFromCtrljob(), controlJobID.getValue()));
        }
        /*---------------------------------------------*/
        /*   Check OperationMode of PortGroup of EQP   */
        /*---------------------------------------------*/
        String portGroupID = aControlJob.getPortGroup();
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        int lenPort = CimArrayUtils.getSize(eqpPortStatuses);
        for (int i = 0; i < lenPort; i++){
            Infos.EqpPortStatus eqpPortStatus = eqpPortStatuses.get(i);
            if (CimStringUtils.equals(portGroupID, eqpPortStatus.getPortGroup())){
                if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE, eqpPortStatus.getOnlineMode())
                        && CimStringUtils.equals(BizConstant.SP_EQP_STARTMODE_MANUAL, eqpPortStatus.getMoveInMode())){
                    log.info("Good Condition");
                } else {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidFromEqpMode(), eqpPortStatus.getOperationMode(), equipmentID.getValue()));
                }
            }
        }
        /*------------------------------------------------------*/
        /*   Check ExecFlg and UpdateFlg of StartCassetteInfo   */
        /*------------------------------------------------------*/
        int lenCas = CimArrayUtils.getSize(strStartCassette);
        for (int i = 0; i < lenCas; i++){
            List<Infos.LotInCassette> lotInCassetteList = strStartCassette.get(i).getLotInCassetteList();
            int lenLot = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < lenLot; j++){
                List<Infos.LotWafer> lotWaferList = lotInCassetteList.get(j).getLotWaferList();
                int lenWafer = CimArrayUtils.getSize(lotWaferList);
                for (int k = 0; k < lenWafer; k++){
                    if (!lotWaferList.get(k).getProcessJobExecFlag() && lotWaferList.get(k).getParameterUpdateFlag()){
                        throw new ServiceException(retCodeConfig.getInvalidInputParam());
                    }
                }
            }
        }
        /*--------------------------------------------------*/
        /*   Set parameterUpdateFlag of StartCassetteInfo   */
        /*--------------------------------------------------*/
        Outputs.ObjControlJobStartReserveInformationOut objControlJobStartReserveInformationOut = controlJobMethod.controlJobStartReserveInformationGet(objCommon, controlJobID, false);
        List<Infos.StartCassette> strSetStartCassette = strStartCassette;
        boolean bParameterUpdate = false;
        lenCas = CimArrayUtils.getSize(strSetStartCassette);
        int lenCJCas = CimArrayUtils.getSize(objControlJobStartReserveInformationOut.getStartCassetteList());
        for (int i = 0; i < lenCJCas; i++){
            List<Infos.LotInCassette> lotInCassetteList = objControlJobStartReserveInformationOut.getStartCassetteList().get(i).getLotInCassetteList();
            int lenCJLot = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < lenCJLot; j++){
                List<Infos.LotWafer> lotWaferList = lotInCassetteList.get(j).getLotWaferList();
                int lenCJWafer = CimArrayUtils.getSize(lotWaferList);
                for (int k = 0; k < lenCJWafer; k++){
                    boolean bBreak = false;
                    for (int I = 0; I < lenCas; I++){
                        List<Infos.LotInCassette> lotInCassetteList1 = strSetStartCassette.get(I).getLotInCassetteList();
                        int lenLot = CimArrayUtils.getSize(lotInCassetteList1);
                        for (int J = 0; J < lenLot; J++){
                            List<Infos.LotWafer> lotWaferList1 = lotInCassetteList1.get(J).getLotWaferList();
                            int lenWafer = CimArrayUtils.getSize(lotWaferList1);
                            for (int K = 0; K < lenWafer; K++){
                                if (ObjectIdentifier.equalsWithValue(lotWaferList.get(k).getWaferID(), lotWaferList1.get(K).getWaferID())){
                                    List<Infos.StartRecipeParameter> startRecipeParameterList = lotWaferList.get(k).getStartRecipeParameterList();
                                    int lenCJRPrm = CimArrayUtils.getSize(startRecipeParameterList);
                                    List<Infos.StartRecipeParameter> startRecipeParameterList1 = lotWaferList1.get(K).getStartRecipeParameterList();
                                    int lenRPrm = CimArrayUtils.getSize(startRecipeParameterList1);
                                    for (int m = 0; m < lenCJRPrm; m++){
                                        for (int M = 0; M < lenRPrm; M++){
                                            if (CimStringUtils.equals(startRecipeParameterList.get(m).getParameterName(), startRecipeParameterList1.get(M).getParameterName())
                                                    && !CimStringUtils.equals(startRecipeParameterList.get(m).getParameterValue(), startRecipeParameterList1.get(M).getParameterValue())){
                                                lotWaferList1.get(K).setParameterUpdateFlag(true);
                                                bParameterUpdate = true;
                                                bBreak = true;
                                                break;
                                            }
                                        }
                                        if (bBreak){
                                            break;
                                        }
                                    }
                                }
                            }
                            if (bBreak){
                                break;
                            }
                        }
                        if (bBreak){
                            break;
                        }
                    }
                }
            }
        }
        /*-------------------------*/
        /*   Check Lot Condition   */
        /*-------------------------*/
        com.fa.cim.newcore.bo.product.CimLot aLot = null;
        lenCas = CimArrayUtils.getSize(strSetStartCassette);
        for (int i = 0; i < lenCas; i++){
            List<Infos.LotInCassette> lotInCassetteList = strSetStartCassette.get(i).getLotInCassetteList();
            int lenLot = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0;  j < lenLot; j++){
                Infos.LotInCassette lotInCassette = lotInCassetteList.get(j);
                if (!lotInCassette.getMoveInFlag()){
                    continue;
                }
                List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
                int lenWafer = CimArrayUtils.getSize(lotWaferList);
                boolean bCheck = false;
                for (int k = 0; k < lenWafer; k++){
                    if (lotWaferList.get(k).getProcessJobExecFlag()){
                        bCheck = true;
                        break;
                    }
                }
                if (bCheck){
                    aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassette.getLotID());
                    //----- check holdState -----
                    String holdState = aLot.getLotHoldState();
                    if (!CimStringUtils.equals(holdState, CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD)){
                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotHoldStat(), lotInCassetteList.get(j).getLotID().getValue()));
                    }
                    //----- check processState -----
                    String processState = aLot.getLotProcessState();
                    if (!CimStringUtils.equals(processState, BizConstant.SP_LOT_PROCSTATE_PROCESSING)){
                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotProcstat(), lotInCassetteList.get(j).getLotID().getValue(), processState));
                    }
                }
            }
        }
        /*---------------------------------------*/
        /*   Check RecipeParameter Information   */
        /*---------------------------------------*/
        lenCas = CimArrayUtils.getSize(strSetStartCassette);
        for (int i = 0; i < lenCas; i++){
            List<Infos.LotInCassette> lotInCassetteList = strSetStartCassette.get(i).getLotInCassetteList();
            int lenLot = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < lenLot; j++){
                Infos.LotInCassette lotInCassette = lotInCassetteList.get(j);
                if (!lotInCassette.getMoveInFlag()){
                    continue;
                }
                /*----------------------------------------------*/
                /*   Get RecipeParameterInfo of LogicalRecipe   */
                /*----------------------------------------------*/
                CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, lotInCassette.getStartRecipe().getLogicalRecipeID());
                Validations.check(aLogicalRecipe == null, new OmCode(retCodeConfig.getNotFoundLogicalRecipe(), lotInCassette.getStartRecipe().getLogicalRecipeID().getValue()));
                CimMachineRecipe aMachineRecipe = null;
                /************************/
                /*   Get subLotType     */
                /************************/
                com.fa.cim.newcore.bo.product.CimLot aLotInner = null;
                String subLotType = null;
                if (CimStringUtils.isEmpty(lotInCassette.getSubLotType())){
                    aLotInner = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassette.getLotID());
                    Validations.check(aLotInner == null, new OmCode(retCodeConfig.getNotFoundLot(), lotInCassette.getLotID().getValue()));
                    subLotType = aLotInner.getSubLotType();
                } else {
                    subLotType = lotInCassette.getSubLotType();
                }
                if (searchCondition == 1){
                    if (aLotInner == null){
                        aLotInner = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassette.getLotID());
                        Validations.check(aLotInner == null, new OmCode(retCodeConfig.getNotFoundLot(), lotInCassette.getLotID().getValue()));
                        aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLotInner, aMachine);
                    }
                } else {
                    aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
                }
                List<RecipeDTO.RecipeParameter> aRecipeParameters = aLogicalRecipe.findRecipeParametersForSubLotType(aMachine, aMachineRecipe, subLotType);
                int lenLRRParms = CimArrayUtils.getSize(aRecipeParameters);
                /*---------------------------------------------------*/
                /*   Check for RecipeParameterChangeType [ByWafer]   */
                /*---------------------------------------------------*/
                if (CimStringUtils.equals(BizConstant.SP_RPARM_CHANGETYPE_BYWAFER, lotInCassette.getRecipeParameterChangeType())){
                    List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
                    int lenWafer = CimArrayUtils.getSize(lotWaferList);
                    for (int k = 0; k < lenWafer; k++){
                        Infos.LotWafer lotWafer = lotWaferList.get(k);
                        if (!lotWafer.getProcessJobExecFlag() && !lotWafer.getParameterUpdateFlag()){
                            continue;
                        }
                        List<Infos.StartRecipeParameter> startRecipeParameterList = lotWafer.getStartRecipeParameterList();
                        int lenRPrm = CimArrayUtils.getSize(startRecipeParameterList);
                        for (int m = 0; m < lenRPrm; m++){
                            for (int x = 0; x < lenLRRParms; x++){
                                if (CimStringUtils.equals(startRecipeParameterList.get(m).getParameterName(), aRecipeParameters.get(x).getParameterName())){
                                    if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_STRING, aRecipeParameters.get(x).getDataType())){
                                        // [String Type] does not need to check.
                                        break;
                                    }
                                    if (aRecipeParameters.get(x).getUseCurrentValueFlag()){
                                        Validations.check(!CimStringUtils.isEmpty(startRecipeParameterList.get(m).getParameterValue()),
                                                new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), aRecipeParameters.get(x).getParameterName()));
                                    } else {
                                        if (CimStringUtils.equals(aRecipeParameters.get(x).getDataType(), BizConstant.SP_DCDEF_VAL_INTEGER)){
                                            long nResult = CimNumberUtils.longValue(startRecipeParameterList.get(m).getParameterValue());
                                            long parameterValue = Long.parseLong(startRecipeParameterList.get(m).getParameterValue());
                                            long lowerLimit = Long.parseLong(aRecipeParameters.get(x).getLowerLimit());
                                            long upperLimit = Long.parseLong(aRecipeParameters.get(x).getUpperLimit());
                                            if (parameterValue < lowerLimit || parameterValue > upperLimit){
                                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(),
                                                        aRecipeParameters.get(x).getParameterName(), aRecipeParameters.get(x).getLowerLimit(), aRecipeParameters.get(x).getUpperLimit()));
                                            }
                                        } else if (CimStringUtils.equals(aRecipeParameters.get(x).getDataType(), BizConstant.SP_DCDEF_VAL_FLOAT)){
                                            Double parameterValue = Double.parseDouble(startRecipeParameterList.get(m).getParameterValue());
                                            Double lowerLimit = Double.parseDouble(aRecipeParameters.get(x).getLowerLimit());
                                            Double upperLimit = Double.parseDouble(aRecipeParameters.get(x).getUpperLimit());
                                            if (parameterValue < lowerLimit || parameterValue > upperLimit){
                                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(),
                                                        aRecipeParameters.get(x).getParameterName(), aRecipeParameters.get(x).getLowerLimit(), aRecipeParameters.get(x).getUpperLimit()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /*--------------------*/
        /*   Update PO Data   */
        /*--------------------*/
        if (bParameterUpdate){
            CimControlJob aControlJob2 = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
            Validations.check(aControlJob2 == null, new OmCode(retCodeConfig.getNotFoundControlJob(), controlJobID.getValue()));
            String portGroupID2 = aControlJob2.getPortGroup();
            processMethod.processStartReserveInformationSet(objCommon, equipmentID, portGroupID2, controlJobID, strSetStartCassette, true);
        }
        /*--------------------------*/
        /*   Set Output Parameter   */
        /*--------------------------*/
        return strSetStartCassette;
    }

    @Override
    public void recipeParameterCJPjConditionCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID) {
        log.info("in-parm's equipmentID : {}", equipmentID);
        log.info("in-parm's controlJobID : {}", controlJobID);
        /*----------------------------------------*/
        /*   Check MultiRecipeCapability of EQP   */
        //----------------------------------------
        log.info("Check MultiRecipeCapability of EQP");
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID)));
        Boolean bProcessJobLevelCtrl = aMachine.isProcessJobLevelControlOn();
        Validations.check(bProcessJobLevelCtrl, new OmCode(retCodeConfig.getPjctrlAvailable(), ObjectIdentifier.fetchValue(equipmentID)));
        String multiRecipeCapability = aMachine.getMultipleRecipeCapability();
        log.info("multiRecipeCapability : {}", multiRecipeCapability);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE, multiRecipeCapability), retCodeConfig.getCassetteEquipmentConditionError());
        /*----------------------*/
        /*   Check ControlJob   */
        /*----------------------*/
        com.fa.cim.newcore.bo.product.CimControlJob aControlJob = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimControlJob.class, controlJobID);
        Validations.check(null == aControlJob, retCodeConfig.getNotFoundControlJob());
        CimMachine aReserveMachine = aControlJob.getMachine();
        ObjectIdentifier resvEqpID = new ObjectIdentifier(aReserveMachine.getIdentifier(), aReserveMachine.getPrimaryKey());
        Validations.check(!ObjectIdentifier.equalsWithValue(equipmentID, resvEqpID), new OmCode(retCodeConfig.getNotFoundEqpFromCtrljob(), controlJobID.getValue()));
        /*---------------------------------------------*/
        /*   Check OperationMode of PortGroup of EQP   */
        /*---------------------------------------------*/
        String portGroupID = aControlJob.getPortGroup();
        log.info("portGroupID : {}", portGroupID);
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        int lenPort = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
        for (int i = 0; i < lenPort; i++) {
            Infos.EqpPortStatus eqpPortStatus = eqpPortInfo.getEqpPortStatuses().get(i);
            String portGroup = eqpPortStatus.getPortGroup();
            if (CimStringUtils.equals(portGroupID, portGroup)) {
                if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE, eqpPortStatus.getOnlineMode())
                        && CimStringUtils.equals(BizConstant.SP_EQP_STARTMODE_MANUAL, eqpPortStatus.getMoveInMode())) {
                    log.info("Good Condition");
                } else {
                    log.error("return RC_INVALID_FROM_EQP_MODE");
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidFromEqpMode(), eqpPortStatus.getOperationMode(), equipmentID.getValue()));
                }
            }
        }
    }

    @Override
    public List<Infos.StartCassette> recipeParameterFillInTxTRC041(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassettes) {
        /*--------------------------------------------*/
        /*   Get StartCassetteInfo from ControlJob.   */
        /*--------------------------------------------*/
        log.info("Get StartCassetteInfo from ControlJob");
        Outputs.ObjControlJobStartReserveInformationOut reserveInformationResult = controlJobMethod.controlJobStartReserveInformationGet(objCommon, controlJobID, false);
        /*----------------------------------------------------------------------------*/
        /*   Set processJobStatus of strStartCassette into                            */
        /*       strStartCassette of strControlJob_startReserveInformation_Get_out.   */
        /*----------------------------------------------------------------------------*/
        int lenCas = CimArrayUtils.getSize(startCassettes);
        log.info("lenCas : {}", lenCas);
        int lenCJCas = CimArrayUtils.getSize(reserveInformationResult.getStartCassetteList());
        log.info("lenCJCas: {}", lenCJCas);
        for (int i = 0; i < lenCJCas; i++) {
            Infos.StartCassette startCassette = reserveInformationResult.getStartCassetteList().get(i);
            int lenCJLot = CimArrayUtils.getSize(startCassette.getLotInCassetteList());
            log.info("lenCJLot : {}", lenCJLot);
            for (int j = 0; j < lenCJLot; j++) {
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(j);
                int lenCJWafer = CimArrayUtils.getSize(lotInCassette.getLotWaferList());
                log.info("lenCJWafer : {}", lenCJWafer);
                for (int k = 0; k < lenCJWafer; k++) {
                    Infos.LotWafer lotWafer = lotInCassette.getLotWaferList().get(k);
                    Boolean bBreak = false;
                    for (int I = 0; I < lenCas; I++) {
                        int lenLot = CimArrayUtils.getSize(startCassettes.get(I).getLotInCassetteList());
                        log.info("lenLot : {}", lenLot);
                        for (int J = 0; J < lenLot; J++) {
                            Infos.LotInCassette lotInCassette1 = startCassettes.get(I).getLotInCassetteList().get(J);
                            int lenWafer = CimArrayUtils.getSize(lotInCassette1.getLotWaferList());
                            log.info("lenWafer : {}", lenWafer);
                            for (int K = 0; K < lenWafer; K++) {
                                Infos.LotWafer lotWafer1 = lotInCassette1.getLotWaferList().get(K);
                                if (ObjectIdentifier.equalsWithValue(lotWafer1.getWaferID(), lotWafer.getWaferID())) {
                                    lotWafer.setProcessJobStatus(lotWafer1.getProcessJobStatus());
                                    bBreak = true;
                                    break;
                                }
                            }
                            if (bBreak) {
                                break;
                            }
                        }
                        if (bBreak) {
                            break;
                        }
                    }
                }
            }
        }
        return reserveInformationResult.getStartCassetteList();
    }

    @Override
    public void recipeBodyFileNameCheckNaming(Infos.ObjCommon objCommon, String fileName) {
        Validations.check(CimObjectUtils.isEmpty(fileName) || fileName.length() > 12, new OmCode(retCodeConfigEx.getInvalidRecipefilename(), "Length error"));

        /*-------------------------*/
        /*   Alpha Numeric Check   */
        /*-------------------------*/
        String checkVal = "^[A-z\\d._-~!#$%@]+$";
        Validations.check(!fileName.matches(checkVal), new OmCode(retCodeConfigEx.getInvalidRecipefilename(), "It includes wrong character"));

        /*-------------------------*/
        /*   "." Position  Check   */
        /*-------------------------*/
        Validations.check(fileName.lastIndexOf(".", 8) <= 0, new OmCode(retCodeConfigEx.getInvalidRecipefilename(), "Period '.' position error"));
    }

    @Override
    public void recipeBodyFileNameCheckDuplicateDR(Infos.ObjCommon objCommon, ObjectIdentifier machineRecipeID, String fileLocation, String fileName) {
        long count = cimJpaRepository.count("SELECT COUNT(ID) FROM OMRCP WHERE RECIPE_ID <> ?1 AND FILE_PATH = ?2 AND FILE_NAME = ?3", ObjectIdentifier.fetchValue(machineRecipeID), fileLocation, fileName);
        Validations.check(count > 0, new OmCode(retCodeConfigEx.getDuplicateRecipefilename(), fileLocation, fileName, ""));
    }

    @Override
    public Outputs.RecipeParameterCheckConditionForAdjustOut recipeParameterCheckConditionForAdjust(Infos.ObjCommon objCommon, Infos.RecipeParameterCheckConditionForAdjustIn in) {

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Outputs.RecipeParameterCheckConditionForAdjustOut recipeParameterCheckConditionForAdjustOut = new Outputs.RecipeParameterCheckConditionForAdjustOut();

        //Trace InParameters
        log.info( "InParam equipmentID   : {}", in.getEquipmentID());
        log.info( "InParam controlJobID  : {}", in.getControlJobID());
        log.info( "InParam strProcessRecipeParameterSeq.length : {} ", CimArrayUtils.getSize(in.getStrProcessRecipeParameterSeq()));

        //-----------------------------------------------------
        // Check and compare inPara waferID and process wafers
        //-----------------------------------------------------
        // Get process Wafer List from each PO
        // Call METHOD processOperation_processWafers_Get (..,strRecipeParamAdjustRptInParm.controlJobID,..)
        log.info("call processOperation_processWafers_Get()");
        List<Infos.ProcessJob> strProcessOperationProcessWafersGetOut = processMethod.processOperationProcessWafersGet(objCommon, in.getControlJobID());

        // Check WaferIDs specified in strLotWaferSeq
        //     All specified WaferIDs must exist inside the process Wafer of the ControlJob


        //Check that each inPara waferID is included in the process wafer list of the controlJob
        //If process job is specified, inParam processJob should be the same as wafer's processJob

        // Result structure
        List<Infos.ProcessRecipeParameter> strProcessRecipeParameterSeq = new ArrayList<>();
        List<Infos.ProcessRecipeParameter> preProcessRecipeParameterSeq = new ArrayList<>();

        int resultCount = 0;
        int lenPJSeq = CimArrayUtils.getSize(strProcessOperationProcessWafersGetOut);
        int lenRecParaSeq = CimArrayUtils.getSize(in.getStrProcessRecipeParameterSeq());
        log.info("lenPJSeq : {}", lenPJSeq);
        log.info("lenRecParaSeq : {}", lenRecParaSeq);
        for (int iCnt1 = 0; iCnt1 < lenPJSeq; iCnt1++) {
            log.info("strProcessJobSeq. iCnt1= : {}", iCnt1);
            for (int iCnt2 = 0; iCnt2 < lenRecParaSeq; iCnt2++) {
                log.info("lenRecParaSeq. iCnt2= : {}", iCnt2);
                int paramCount = 0;
                log.info( "strProcessOperation_processWafers_Get_out.strProcessJobSeq[iCnt1].processJobID : {}", strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessJobID());
                log.info( "strRecipeParameter_CheckConditionForAdjust_in.strProcessRecipeParameterSeq[iCnt2].processJobID : {}", in.getStrProcessRecipeParameterSeq().get(iCnt2).getProcessJobID());
                if ( CimStringUtils.equals(strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessJobID(),
                        in.getStrProcessRecipeParameterSeq().get(iCnt2).getProcessJobID())) {
                    log.info( "Same processJobID");
                    Infos.ProcessRecipeParameter tmpProcessRecipeParameter = in.getStrProcessRecipeParameterSeq().get(iCnt2);
                    Infos.ProcessRecipeParameter strProcessRecipeParameter = new Infos.ProcessRecipeParameter();
                    BeanUtils.copyProperties(tmpProcessRecipeParameter, strProcessRecipeParameter);
                    strProcessRecipeParameterSeq.add(strProcessRecipeParameter);
                    Infos.ProcessRecipeParameter tmpPreProcessRecipeParameter = new Infos.ProcessRecipeParameter();
                    BeanUtils.copyProperties(strProcessRecipeParameterSeq.get(resultCount), tmpPreProcessRecipeParameter);
                    preProcessRecipeParameterSeq.add(tmpPreProcessRecipeParameter);

                    // check reported waferIDs is the same
                    log.info("strProcessOperation_processWafers_Get_out.strProcessJobSeq[iCnt1].strProcessWaferList.length() : {}", CimArrayUtils.getSize(strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessWaferList()));
                    log.info("strRecipeParameter_CheckConditionForAdjust_in.strProcessRecipeParameterSeq[iCnt2].waferIDs.length() ; {}", CimArrayUtils.getSize(in.getStrProcessRecipeParameterSeq().get(iCnt2).getWaferIDs()));
                    Validations.check(CimArrayUtils.getSize(strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessWaferList()) !=
                            CimArrayUtils.getSize(in.getStrProcessRecipeParameterSeq().get(iCnt2).getWaferIDs()), retCodeConfig.getInvalidParameterWithMsg());

                    // Check strRecipeParameter_CheckConditionForAdjust_in.strProcessRecipeParameterSeq[iCnt2]..waferIDs = strProcessOperation_processWafers_Get_out..strProcessJobSeq[iCnt1]..strProcessWaferList
                    Boolean bFoundFlag = false;
                    int lenInProcWafer = CimArrayUtils.getSize(in.getStrProcessRecipeParameterSeq().get(iCnt2).getWaferIDs());
                    log.info("lenInProcWafer : {}", lenInProcWafer);
                    for (int iCnt3 = 0; iCnt3 < lenInProcWafer; iCnt3++) {
                        log.info( "iCnt3 : {}", iCnt3);
                        log.info("waferID from strRecipeParameter_CheckConditionForAdjust_in : {}", in.getStrProcessRecipeParameterSeq().get(iCnt2).getWaferIDs().get(iCnt3));
                        bFoundFlag = false;

                        int lenProcWafer = CimArrayUtils.getSize(strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessWaferList());
                        log.info("lenProcWafer : {}", lenProcWafer);
                        for (int iCnt4 = 0; iCnt4 < lenProcWafer; iCnt4++) {
                            log.info("iCnt4 : {}", iCnt4);
                            log.info( "waferID from strProcessOperation_processWafers_Get_out :  {}", strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessWaferList().get(iCnt4).getWaferID());
                            if (ObjectIdentifier.equalsWithValue(strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessWaferList().get(iCnt4).getWaferID(),
                                    in.getStrProcessRecipeParameterSeq().get(iCnt2).getWaferIDs().get(iCnt3))) {
                                bFoundFlag = true;
                                break;
                            }
                        }

                        Validations.check(!bFoundFlag, retCodeConfig.getInvalidParameterWithMsg());
                    }
                    // wafer list in the same processJob should share the same recipe parameters
                    // so only get recipe parameter from the first wafer

                    // Get lot object from first lot
                    ObjectIdentifier tmpWaferID = strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessWaferList().get(0).getWaferID();
                    log.info("tmpWaferID : {}", tmpWaferID);
                    log.info("lotID {}", strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessWaferList().get(0).getLotID());
                    com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,strProcessOperationProcessWafersGetOut.get(iCnt1).getProcessWaferList().get(0).getLotID());
                    Validations.check(null == aLot, retCodeConfig.getNotFoundLot());

                    // Get lot process operation
                    com.fa.cim.newcore.bo.pd.CimProcessOperation aPosPO = aLot.getProcessOperation();
                    Validations.check(null == aPosPO, retCodeConfig.getNotFoundProcessOperation());

                    // Get current recipe parameters in PO
                    List<ProcessDTO.StartRecipeParameterSetInfo> aStartRecipeParameterSetInfoSeq = aPosPO.getAssignedRecipeParameterSets();

                    // Compare inparam recipe parameter and current erecipe parameters value

                    int lenStartRecPara = CimArrayUtils.getSize(aStartRecipeParameterSetInfoSeq);
                    List<Infos.StartRecipeParameter> startRecipeParameters = new ArrayList<>();
                    List<Infos.StartRecipeParameter> preStartRecipeParameters = new ArrayList<>();
                    Infos.ProcessRecipeParameter processRecipeParameter = strProcessRecipeParameterSeq.get(resultCount);
                    Infos.ProcessRecipeParameter preProcessRecipeParameter = preProcessRecipeParameterSeq.get(resultCount);
                    processRecipeParameter.setStartRecipeParameterList(startRecipeParameters);
                    preProcessRecipeParameter.setStartRecipeParameterList(preStartRecipeParameters);
                    log.info("lenStartRecPara : {}", lenStartRecPara);
                    for (int iCnt3 = 0; iCnt3 < lenStartRecPara; iCnt3++) {
                        log.info("iCnt3 : {}", iCnt3);
                        // Apply for each wafer data
                        int lenStartRecPara1 = CimArrayUtils.getSize(aStartRecipeParameterSetInfoSeq.get(iCnt3).getApplyWaferInfoList());
                        log.info("lenStartRecPara1 : {}", lenStartRecPara1);
                        for (int iCnt4 = 0; iCnt4 < lenStartRecPara1; iCnt4++) {
                            log.info("iCnt4 : {}", iCnt4);
                            log.info( "tmpWaferID.identifier : {}", tmpWaferID);
                            log.info( "aStartRecipeParameterSetInfoSeq[iCnt3].applyWafers[iCnt4].waferID : {}", aStartRecipeParameterSetInfoSeq.get(iCnt3).getApplyWaferInfoList().get(iCnt4).getWaferID());
                            if (ObjectIdentifier.equalsWithValue(tmpWaferID, aStartRecipeParameterSetInfoSeq.get(iCnt3).getApplyWaferInfoList().get(iCnt4).getWaferID())) {
                                log.info( "found the wafer : {}", tmpWaferID);
                                // found the wafer
                                List<Infos.StartRecipeParameter>  strStartRecipeParameterSeq = in.getStrProcessRecipeParameterSeq().get(iCnt2).getStartRecipeParameterList();
                                int lenStartRecPara3 = CimArrayUtils.getSize(strStartRecipeParameterSeq);
                                log.info( "lenStartRecPara3 : {}", lenStartRecPara3);
                                for (int iCnt6 = 0; iCnt6 < lenStartRecPara3; iCnt6++) {
                                    log.info("iCnt6 : {}", iCnt6);
                                    // Compare inpara with recipeParameters
                                    Boolean parameterFoundFlag = false;
                                    int lenStartRecPara2 = CimArrayUtils.getSize(aStartRecipeParameterSetInfoSeq.get(iCnt3).getRecipeParameterList());
                                    log.info("lenStartRecPara2 : {}", lenStartRecPara2);
                                    for (int iCnt5 = 0; iCnt5 < lenStartRecPara2; iCnt5++) {
                                        log.info("iCnt5 : {}", iCnt5);
                                        String tmpParameterName = strStartRecipeParameterSeq.get(iCnt6).getParameterName();
                                        log.info("tmpParameterName : {}", tmpParameterName);
                                        log.info( "aStartRecipeParameterSetInfoSeq[iCnt3].recipeParameters[iCnt5].parameterName : {}", aStartRecipeParameterSetInfoSeq.get(iCnt3).getRecipeParameterList().get(iCnt5).getParameterName());
                                        if (CimStringUtils.equals(tmpParameterName, aStartRecipeParameterSetInfoSeq.get(iCnt3).getRecipeParameterList().get(iCnt5).getParameterName())) {
                                            // found the recipe parameter
                                            log.info( "found the recipe parameter : {}", tmpParameterName);
                                            parameterFoundFlag = true;
                                            log.info("aStartRecipeParameterSetInfoSeq[iCnt3].recipeParameters[iCnt5].parameterValue : {}", aStartRecipeParameterSetInfoSeq.get(iCnt3).getRecipeParameterList().get(iCnt5).getParameterValue());
                                            log.info("strStartRecipeParameterSeq[iCnt6].parameterValue : {}", strStartRecipeParameterSeq.get(iCnt6).getParameterValue());
                                            if ( !CimStringUtils.equals(aStartRecipeParameterSetInfoSeq.get(iCnt3).getRecipeParameterList().get(iCnt5).getParameterValue(),
                                                    strStartRecipeParameterSeq.get(iCnt6).getParameterValue() )) {
                                                log.info("parameterValue is not the same");
                                                // set result structure
                                                Infos.StartRecipeParameter tmpStartRecipeParameter = strStartRecipeParameterSeq.get(iCnt6);
                                                startRecipeParameters.add(tmpStartRecipeParameter);
                                                Infos.StartRecipeParameter preStartRecipeParameter = new Infos.StartRecipeParameter();
                                                BeanUtils.copyProperties(tmpStartRecipeParameter, preStartRecipeParameter);
                                                preStartRecipeParameter.setParameterValue(aStartRecipeParameterSetInfoSeq.get(iCnt3).getRecipeParameterList().get(iCnt5).getParameterValue());
                                                preStartRecipeParameter.setUseCurrentSettingValueFlag(aStartRecipeParameterSetInfoSeq.get(iCnt3).getRecipeParameterList().get(iCnt5).getUseCurrentSettingValueFlag());
                                                preStartRecipeParameters.add(preStartRecipeParameter);
                                            }
                                            log.info("break : {}", iCnt6);
                                            break;
                                        }
                                    }
                                    Validations.check(!parameterFoundFlag, new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "Invalid recipeparameter is reported."));
                                }
                            }
                        }
                    } // for PO recipe parameter set
                    resultCount++;
                } // if found the reported processJobID
            } // for reported processJobs
        } // for all process jobs

        //Check changed recipe parameters
        //Get recipe parameter setting from Equipment

        log.info("Call equipment_recipeParameterInfo_GetDR");
        List<Infos.RecipeParameterInfo> recipeParameterInfo = recipeMethod.recipeParameterInfoGetDR(objCommon, in.getEquipmentID());

        //  There must be parameter value within lower limit and upper limit when dataType is SP_DCDef_Val_Integer or SP_DCDef_Val_Float
        int lenRecipePara = CimArrayUtils.getSize(strProcessRecipeParameterSeq);
        int lenStartRecipeParaInfo = CimArrayUtils.getSize(recipeParameterInfo);
        log.info("lenRecipePara : {}", lenRecipePara);
        log.info( "lenStartRecipeParaInfo : {}", lenStartRecipeParaInfo);
        for (int iCnt7 = 0; iCnt7 < lenRecipePara; iCnt7++) {
            log.info( "iCnt7 : {}", iCnt7);
            int lenStartRecipePara = CimArrayUtils.getSize(strProcessRecipeParameterSeq.get(iCnt7).getStartRecipeParameterList());
            log.info("lenStartRecipePara : {}", lenStartRecipePara);
            for (int iCnt8 = 0; iCnt8 < lenStartRecipePara; iCnt8++) {
                log.info("iCnt8 : {}", iCnt8);
                String tmpParameterName = strProcessRecipeParameterSeq.get(iCnt7).getStartRecipeParameterList().get(iCnt8).getParameterName();
                log.info("tmpParameterName : {}", tmpParameterName);
                for (int iCnt9 = 0; iCnt9 < lenStartRecipeParaInfo; iCnt9++) {
                    log.info("iCnt9 : {}", iCnt9);
                    Infos.RecipeParameterInfo strRecipeParameterInfo = recipeParameterInfo.get(iCnt9);
                    log.info("strRecipeParameterInfo.parameterName :  {}", strRecipeParameterInfo.getParameterName());
                    if (CimStringUtils.equals(tmpParameterName, strRecipeParameterInfo.getParameterName() )) {
                        // useCurrentSettingValueFlag cannot be true
                        // Original value (stored in PO before parameter adjustment) is checked
                        log.info( "same parameterName");
                        Validations.check(preProcessRecipeParameterSeq.get(iCnt7).getStartRecipeParameterList().get(iCnt8).getUseCurrentSettingValueFlag(),
                                new OmCode(retCodeConfigEx.getInvalidParametervalueUsecurrent(), tmpParameterName));
                        // found the recipe parameter
                        log.info("strRecipeParameterInfo.parameterDataType : {}", strRecipeParameterInfo.getParameterDataType());
                        if ( CimStringUtils.equals(strRecipeParameterInfo.getParameterDataType(), BizConstant.SP_DCDEF_VAL_INTEGER) ) {
                            log.info("dataType = SP_DCDef_Val_Integer");
                            Boolean bIsLong = true;
                            int parameterValue = 0;
                            int lowerLimit     = 0;
                            int upperLimit     = 0;
                            try {
                                parameterValue = Integer.parseInt(String.format(strProcessRecipeParameterSeq.get(iCnt7).getStartRecipeParameterList().get(iCnt8).getParameterValue(), "%d"));
                            } catch (NumberFormatException e) {
                                bIsLong = false;
                            }
                            lowerLimit = Integer.parseInt(String.format(strRecipeParameterInfo.getParameterLowerLimit(), "%d"));
                            upperLimit = Integer.parseInt(String.format(strRecipeParameterInfo.getParameterUpperLimit(), "%d"));
                            log.info("lowerLimit {}", lowerLimit);
                            log.info( "upperLimit {}", upperLimit);
                            log.info( "parameterValue {}", parameterValue);
                            Validations.check(!bIsLong || ((parameterValue < lowerLimit) || (parameterValue > upperLimit)),
                                    new OmCode(retCodeConfig.getInvalidParameterValueRange(), strRecipeParameterInfo.getParameterName(), strRecipeParameterInfo.getParameterLowerLimit(), strRecipeParameterInfo.getParameterUpperLimit()));
                        }
                        else if ( CimStringUtils.equals(strRecipeParameterInfo.getParameterDataType(), BizConstant.SP_DCDEF_VAL_FLOAT) ) {
                            log.info("dataType is SP_DCDef_Val_Float");
                            Double parameterValue = 0.0;
                            Double lowerLimit     = 0.0;
                            Double upperLimit     = 0.0;
                            parameterValue = Double.valueOf(String.format(strProcessRecipeParameterSeq.get(iCnt7).getStartRecipeParameterList().get(iCnt8).getParameterValue(), "%lf"));
                            lowerLimit = Double.valueOf(String.format(strRecipeParameterInfo.getParameterLowerLimit(), "%lf"));
                            upperLimit = Double.valueOf(String.format(strRecipeParameterInfo.getParameterUpperLimit(), "%lf"));
                            log.info("lowerLimit {}", lowerLimit);
                            log.info( "upperLimit {}", upperLimit);
                            log.info( "parameterValue {}", parameterValue);
                            Validations.check((parameterValue < lowerLimit) || (parameterValue > upperLimit),
                                    new OmCode(retCodeConfig.getInvalidParameterValueRange(), strRecipeParameterInfo.getParameterName(), strRecipeParameterInfo.getParameterLowerLimit(), strRecipeParameterInfo.getParameterUpperLimit()));
                        }
                        break;
                    }
                }
            } // for recipe parameter list level
        } // for process Job level

        // Set Result
        recipeParameterCheckConditionForAdjustOut.setEquipmentID(in.getEquipmentID());
        recipeParameterCheckConditionForAdjustOut.setControlJobID(in.getControlJobID());
        recipeParameterCheckConditionForAdjustOut.setStrProcessRecipeParameterSeq(strProcessRecipeParameterSeq);
        recipeParameterCheckConditionForAdjustOut.setPreProcessRecipeParameterSeq(preProcessRecipeParameterSeq);

        return recipeParameterCheckConditionForAdjustOut;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/24 16:46
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjRecipeParameterCheckConditionForStoreOut recipeParameterCheckConditionForStore(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.LotWafer> strLotWaferSeq) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Outputs.ObjRecipeParameterCheckConditionForStoreOut out = new Outputs.ObjRecipeParameterCheckConditionForStoreOut();

        //-----------------------------------------------------
        // Check and compare inPara waferID and process wafers
        //-----------------------------------------------------
        //Get process Wafer List from each PO
        log.info("call processOperation_processWafers_Get()");
        List<Infos.ProcessJob> processWafersRetCode = processMethod.processOperationProcessWafersGet(objCommon, controlJobID);
        // Check WaferIDs specified in strLotWaferSeq
        // All specified WaferIDs must exist inside the process Wafer of the ControlJob
        int iCnt1 = 0;
        int iCnt2 = 0;
        int iCnt3 = 0;
        int iCnt4 = 0;
        int iCnt5 = 0;
        int iCnt6 = 0;
        int iCnt7 = 0;
        Boolean foundWaferFlag = false;
        List<ObjectIdentifier> reportedLotIDs = new ArrayList<>();
        int reportLotCnt = 0;
        int lenWaferInParam = CimArrayUtils.getSize(strLotWaferSeq);
        int lenProcJobSeq = CimArrayUtils.getSize(processWafersRetCode);
        for (iCnt1 = 0; iCnt1 < lenWaferInParam; iCnt1++) {
            for (iCnt2 = 0; iCnt2 < lenProcJobSeq; iCnt2++) {
                int lenWaferListInPJSeq = CimArrayUtils.getSize(processWafersRetCode.get(iCnt2).getProcessWaferList());
                for (iCnt3 = 0; iCnt3 < lenWaferListInPJSeq; iCnt3++) {
                    if (ObjectIdentifier.equalsWithValue(strLotWaferSeq.get(iCnt1).getWaferID(),
                            processWafersRetCode.get(iCnt2).getProcessWaferList().get(iCnt3).getWaferID())) {
                        log.info("foundWaferFlag = TRUE");
                        foundWaferFlag = true;
                        Boolean foundLotFlag = true;
                        //if objProcessOperation_processWafers_Get_out..strProcessWaferSeq[iCnt2].lotID is not in reportedLotIDs
                        //add the lotID to reportedLotIDs.
                        for (iCnt4 = 0; iCnt4 < reportLotCnt; iCnt4++) {
                            if (ObjectIdentifier.equalsWithValue(processWafersRetCode.get(iCnt2).getProcessWaferList().get(iCnt3).getLotID(),
                                    reportedLotIDs.get(iCnt4))) {
                                log.info("foundLotFlag == FALSE");
                                foundLotFlag = false;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isTrue(foundLotFlag)) {
                            reportedLotIDs.add(processWafersRetCode.get(iCnt2).getProcessWaferList().get(iCnt3).getLotID());
                            reportLotCnt++;
                        }
                    }
                }
            }
            if(!foundWaferFlag){
                //Set error message MSG_INVALID_PARAMETER_WITH_MSG ("wafer xx is not one of the start wafers in this controlJob");
                StringBuffer errorMsgSb = new StringBuffer();
                errorMsgSb.append("wafer is not one of the start wafers in this controlJob.")
                        .append(" wafer: ").append(ObjectIdentifier.fetchValue(strLotWaferSeq.get(iCnt1).getWaferID()));
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterWithMsg(), errorMsgSb.toString()));
            }
        }
        List<Infos.LotStartRecipeParameter> strLotStartRecipeParameterSeq = new ArrayList<>();
        out.setStrLotStartRecipeParameterSeq(strLotStartRecipeParameterSeq);
        //-----------------------------------------------------
        // Check, compare and merge recipe parameters
        //-----------------------------------------------------
        for (iCnt1 = 0; iCnt1 < reportedLotIDs.size(); iCnt1++) {
            // Set Result structure
            Infos.LotStartRecipeParameter lotStartRecipeParameter = new Infos.LotStartRecipeParameter();
            strLotStartRecipeParameterSeq.add(iCnt1, lotStartRecipeParameter);
            lotStartRecipeParameter.setLotID(reportedLotIDs.get(iCnt1));
            lotStartRecipeParameter.setRecipeParameterChangeType(BizConstant.SP_RPARM_CHANGETYPE_BYWAFER);

            // Get Lot Object
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, reportedLotIDs.get(iCnt1));
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), reportedLotIDs.get(iCnt1).getValue()));
            //Get lot process Operation
            com.fa.cim.newcore.bo.pd.CimProcessOperation aPO = aLot.getProcessOperation();
            Validations.check(aPO == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), "", reportedLotIDs.get(iCnt1).getValue()));
            // Get current recipe parameters in PO
            List<ProcessDTO.StartRecipeParameterSetInfo> aStartRecipeParameterSetInfoSeq = aPO.getAssignedRecipeParameterSets();
            // Get all current recipe parameters and merge with reported recipe parameters
            List<Infos.LotWafer> strLotWaferList = new ArrayList<>();
            int lotWaferSeqCount = 0;
            for (iCnt2 = 0; iCnt2 < CimArrayUtils.getSize(aStartRecipeParameterSetInfoSeq); iCnt2++) {
                // Apply for each wafer data
                for (iCnt3 = 0; iCnt3 < CimArrayUtils.getSize(aStartRecipeParameterSetInfoSeq.get(iCnt2).getApplyWaferInfoList()); iCnt3++) {
                    ObjectIdentifier tmpWaferID = aStartRecipeParameterSetInfoSeq.get(iCnt2).getApplyWaferInfoList().get(iCnt3).getWaferID();
                    Infos.LotWafer reportedLotWafer = new Infos.LotWafer();
                    Boolean reportedWaferFlag = false;
                    // check if this wafer is reported wafer (recipe parameter change wafer)
                    for (iCnt4 = 0; iCnt4 < CimArrayUtils.getSize(strLotWaferSeq); iCnt4++) {
                        if (CimStringUtils.equals(tmpWaferID.getValue(), strLotWaferSeq.get(iCnt4).getWaferID().getValue())) {
                            log.info("reportedWaferFlag = TRUE");
                            reportedWaferFlag = true;
                            reportedLotWafer = strLotWaferSeq.get(iCnt4);
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(reportedWaferFlag)) {
                        //check reported recipe parameters
                        //length of the recipe parameter count should be the same
                        Validations.check(CimArrayUtils.getSize(reportedLotWafer.getStartRecipeParameterList()) != CimArrayUtils.getSize(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList()),
                                retCodeConfig.getInvalidParameterWithMsg());
                        //use_flg (useCurrentValueFlag) should NOT be TRUE if value is changed
                        for (iCnt5 = 0; iCnt5 < CimArrayUtils.getSize(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList()); iCnt5++) {
                            if (CimBooleanUtils.isFalse(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList().get(iCnt5).getUseCurrentSettingValueFlag())) {
                                // No need to check
                                log.info("no need to check");
                                continue;
                            } else {
                                log.info("seCurrentSettingValueFlag = TRUE");
                                String tmpParameterName = aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList().get(iCnt5).getParameterName();
                                for (iCnt6 = 0; iCnt6 < CimArrayUtils.getSize(reportedLotWafer.getStartRecipeParameterList()); iCnt6++) {
                                    if (CimStringUtils.equals(tmpParameterName, reportedLotWafer.getStartRecipeParameterList().get(iCnt6).getParameterName())) {
                                        log.info("seCurrentSettingValueFlag = TRUE");
                                        reportedLotWafer.getStartRecipeParameterList().get(iCnt6).setUseCurrentSettingValueFlag(true);
                                        Validations.check(!CimStringUtils.equals(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList().get(iCnt5).getParameterValue(),
                                                reportedLotWafer.getStartRecipeParameterList().get(iCnt6).getParameterValue()),
                                                new OmCode(retCodeConfigEx.getInvalidParametervalueUsecurrent(), reportedLotWafer.getStartRecipeParameterList().get(iCnt6).getParameterName()));
                                        break;
                                    }
                                }
                            }
                        }
                        //set reported recipe parameters
                        strLotWaferList.add(lotWaferSeqCount, reportedLotWafer);
                    } else {
                        // copy existing recipe parameters
                        Infos.LotWafer lotWafer = new Infos.LotWafer();
                        strLotWaferList.add(lotWaferSeqCount, lotWafer);
                        lotWafer.setWaferID(tmpWaferID);
                        lotWafer.setSlotNumber(aStartRecipeParameterSetInfoSeq.get(iCnt2).getApplyWaferInfoList().get(iCnt3).getSlotNumber());
                        lotWafer.setControlWaferFlag(aStartRecipeParameterSetInfoSeq.get(iCnt2).getApplyWaferInfoList().get(iCnt3).isControlWaferFlag());
                        lotWafer.setProcessJobExecFlag(true);
                        lotWafer.setProcessJobStatus("");
                        List<Infos.StartRecipeParameter> startRecipeParameterList = new ArrayList<>();
                        lotWafer.setStartRecipeParameterList(startRecipeParameterList);
                        for (iCnt7 = 0; iCnt7 < CimArrayUtils.getSize(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList()); iCnt7++) {
                            //convert aStartRecipeParameterSetInfoSeq[iCnt2].recipeParameters to strLotWaferSeq[lotWaferSeqCount].strStartRecipeParameter
                            Infos.StartRecipeParameter startRecipeParameter = new Infos.StartRecipeParameter();
                            startRecipeParameterList.add(iCnt7, startRecipeParameter);
                            startRecipeParameter.setParameterName(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList().get(iCnt7).getParameterName());
                            startRecipeParameter.setParameterValue(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList().get(iCnt7).getParameterValue());
                            startRecipeParameter.setTargetValue(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList().get(iCnt7).getTargetValue());
                            startRecipeParameter.setUseCurrentSettingValueFlag(aStartRecipeParameterSetInfoSeq.get(iCnt2).getRecipeParameterList().get(iCnt7).getUseCurrentSettingValueFlag());
                        }
                    }
                    lotWaferSeqCount++;
                }
            }
            //Set Result structure
            out.getStrLotStartRecipeParameterSeq().get(iCnt1).setStrLotWaferSeq(strLotWaferList);
        }
        return out;
    }

    @Override
    public Boolean recipeBodyManageFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        /*--------------------------*/
        /*   Get eqp Object   */
        /*--------------------------*/
        //eqp eqp = equipmentCore.convertEqpIDToEquipment(equipmentID);
        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == equipment, retCodeConfig.getNotFoundEquipment());
        /*-----------------------------------------*/
        /*   Get Special eqp Control Value   */
        /*-----------------------------------------*/
        return equipment.isRecipeBodyManageFlagOn();
    }


    @Override
    public List<Infos.RecipeParameterInfo> recipeParameterInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        List<Infos.RecipeParameterInfo> recipeParameterInfoList = new ArrayList<>();
        String sql = "   SELECT IDX_NO, RPARAM_NAME, RPARAM_UNIT, RPARAM_DATA_TYPE, RPARAM_LOWER_LIMIT, RPARAM_UPPER_LIMIT, RPARAM_USE_CUR_FLAG, RPARAM_DEFAULT \n" +
                "            FROM OMEQP, OMEQP_RPARAM \n" +
                "            WHERE OMEQP.EQP_ID = ?1 \n" +
                "             AND OMEQP.ID = OMEQP_RPARAM.REFKEY";
        List<Object[]> eqpAndEqpRparmByEqpId = cimJpaRepository.query(sql, ObjectIdentifier.fetchValue(equipmentID));
        if (!CimArrayUtils.isEmpty(eqpAndEqpRparmByEqpId)) {
            for (Object[] objects : eqpAndEqpRparmByEqpId) {
                Infos.RecipeParameterInfo recipeParameterInfo = new Infos.RecipeParameterInfo();
                recipeParameterInfo.setSequenceNumber(Long.valueOf(CimObjectUtils.toString(objects[0])));
                recipeParameterInfo.setParameterName(CimObjectUtils.toString(objects[1]));
                recipeParameterInfo.setParameterUnit(CimObjectUtils.toString(objects[2]));
                recipeParameterInfo.setParameterDataType(CimObjectUtils.toString(objects[3]));
                recipeParameterInfo.setParameterLowerLimit(CimObjectUtils.toString(objects[4]));
                recipeParameterInfo.setParameterUpperLimit(CimObjectUtils.toString(objects[5]));
                recipeParameterInfo.setUseCurrentSettingValueFlag(CimBooleanUtils.convert(objects[6]));
                recipeParameterInfo.setParameterTargetValue(CimObjectUtils.toString(objects[7]));
                recipeParameterInfo.setParameterValue(CimObjectUtils.toString(objects[7]));
                recipeParameterInfoList.add(recipeParameterInfo);
            }
        }

        return recipeParameterInfoList;
    }

}
