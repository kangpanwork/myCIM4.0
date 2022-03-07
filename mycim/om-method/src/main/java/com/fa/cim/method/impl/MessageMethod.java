package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.entity.nonruntime.CimCarrierOutReqQueueDO;
import com.fa.cim.entity.nonruntime.CimMessageQueueDO;
import com.fa.cim.entity.nonruntime.fmc.CimSLMMessageQueueDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IMessageMethod;
import com.fa.cim.newcore.bo.code.CimSystemMessageCode;
import com.fa.cim.newcore.bo.code.CodeManager;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.msgdistribution.CimMessageDefinition;
import com.fa.cim.newcore.bo.msgdistribution.MessageDistributionManager;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.msgdistribution.MessageDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import com.fa.cim.newcore.standard.prcssdfn.ProcessFlow;
import com.fa.cim.newcore.standard.prsnmngm.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/6       ********              lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/8/6 22:36
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class MessageMethod implements IMessageMethod {

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    @Qualifier("CodeManagerCore")
    private CodeManager codeManager;

    @Autowired
    @Qualifier("MessageDistributionManagerCore")
    private MessageDistributionManager messageDistributionManager;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Override
    public ObjectIdentifier systemMessageDefinitionMessageIDGetByMessageCode(Infos.ObjCommon objCommon, String subSystemID, String messageCode) {
        /*------------------------------------*/
        /*   Get System Message Code Object   */
        /*------------------------------------*/
        String sysMsgID = subSystemID + "." + messageCode;
        CimSystemMessageCode aSysMsgDef = codeManager.findSystemMessageCodeNamed(sysMsgID);
        Validations.check(aSysMsgDef == null, new OmCode(retCodeConfigEx.getNotFoundSysMsgDef(), messageCode, subSystemID));

        /*-----------------------------------*/
        /*   Get Message Definition Object   */
        /*-----------------------------------*/
        CimMessageDefinition aMsgDef = aSysMsgDef.getMessageDefinition();
        /*----------------------------*/
        /*   Set Returned Structure   */
        /*----------------------------*/
        if (aMsgDef == null){
            return null;
        }
        return new ObjectIdentifier(aMsgDef.getIdentifier(), aMsgDef.getPrimaryKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    // need to open new Transaction
    public void messageDistributionMgrPutMessage(Infos.ObjCommon objCommon, ObjectIdentifier messageID, ObjectIdentifier lotID, String lotStatus, ObjectIdentifier equipmentID, ObjectIdentifier routeID, String operationNumber, String reasonCode, String messageText) {
        /*----------------------*/
        /*   Check Input Parm   */
        /*----------------------*/
        Validations.check(ObjectIdentifier.isEmptyWithValue(messageID), retCodeConfig.getInvalidInputParam());
        CimLot aLot = null;
        long messageLen = CimStringUtils.length(messageText);
        Validations.check(messageLen > 2048, retCodeConfig.getInvalidInputParam());
        /*-----------------------------------------------*/
        /*   Set Request Data by using Input Parameter   */
        /*-----------------------------------------------*/
        MessageDTO.MessageRequest reqData = new MessageDTO.MessageRequest();
        reqData.setFloorEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        reqData.setMessageID(messageID.getValue());
        reqData.setLotID(ObjectIdentifier.fetchValue(lotID));
        reqData.setLotStatus(lotStatus);
        reqData.setReasonCode(reasonCode);
        reqData.setUserID(objCommon.getUser().getUserID().getValue());
        reqData.setOperationNumber(operationNumber);
        reqData.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
        reqData.setRouteID(ObjectIdentifier.fetchValue(routeID));
        reqData.setMessageText(messageText);
        /*-------------------------------------*/
        /*   Get Lot Object to set Lot Owner   */
        /*-------------------------------------*/
        if (!ObjectIdentifier.isEmptyWithValue(lotID)){
            aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            if (aLot != null){
                CimPerson lotOwnerVar = aLot.getLotOwner();
                if (lotOwnerVar != null){
                    reqData.setLotOwner(lotOwnerVar.getIdentifier());
                }
                if (CimStringUtils.isEmpty(reqData.getLotStatus())){
                    reqData.setLotStatus(aLot.getState());
                }
                if (CimStringUtils.isEmpty(reqData.getOperationName())){
                    reqData.setOperationNumber(aLot.getOperationNumber());
                }
                if (CimStringUtils.isEmpty(reqData.getRouteID())){
                    CimProcessDefinition aMainPD = aLot.getMainProcessDefinition();
                    if (aMainPD != null){
                        reqData.setRouteID(aMainPD.getIdentifier());
                    }
                }
            }
        }
        /*-------------------------------------------------*/
        /*   Get Equipment Object to set Equipment Owner   */
        /*-------------------------------------------------*/
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)){
            boolean isStorageBool = false;
            CimMachine aBaseMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
            Validations.check(aBaseMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
            isStorageBool = aBaseMachine.isStorageMachine();
            Person aPerson = aBaseMachine.getOwner();
            Validations.check(aPerson == null, retCodeConfig.getSystemError());
            reqData.setEquipmentOwner(aPerson.getIdentifier());
        }
        /*-------------------------------------------*/
        /*   Get Main PD Object to set Route Owner   */
        /*-------------------------------------------*/
        if (!CimStringUtils.isEmpty(reqData.getRouteID())){
            ObjectIdentifier tmpRouteID = new ObjectIdentifier(reqData.getRouteID(),"");
            CimProcessDefinition aProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class, tmpRouteID);
            Validations.check(aProcessDefinition == null, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), tmpRouteID.getValue()));
            if (aLot != null){
                Person aPerson = aProcessDefinition.getOwner();
                if (aPerson != null){
                    reqData.setRouteOwner(aPerson.getIdentifier());
                } else {
                    throw new ServiceException(retCodeConfig.getSystemError());
                }
            }
            /*--------------------------------------------------*/
            /*   Get PD Object to set PDID and Operation Name   */
            /*--------------------------------------------------*/
            if (!CimStringUtils.isEmpty(reqData.getOperationNumber())){
                CimProcessFlow aProcessFlow = aProcessDefinition.getActiveMainProcessFlow();
                Validations.check(aProcessFlow == null, new OmCode(retCodeConfig.getNotFoundProcessFlow(),""));
                if (aProcessFlow.isNewlyCreated()){
                    if (aLot != null){
                        ProcessFlow aPF = aLot.getProcessFlow();
                        aProcessFlow = (CimProcessFlow) aPF;
                        Validations.check(aProcessFlow == null, new OmCode(retCodeConfig.getNotFoundProcessFlow(), ""));
                    } else {
                        throw new ServiceException(new OmCode(retCodeConfig.getNotFoundProcessFlow(), ""));
                    }
                }
                AtomicReference<CimProcessFlow> aOutMainPF = new AtomicReference<>();
                AtomicReference<CimProcessFlow> aOutModulePF = new AtomicReference<>();
                CimProcessOperationSpecification aPOS = aProcessFlow.getProcessOperationSpecificationFor(reqData.getOperationNumber(), aOutMainPF, aOutModulePF);
                if (aPOS != null){
                    List<ProcessDefinition> aProcessDefList = aPOS.getProcessDefinitions();
                    CimProcessDefinition aPosPD = (CimProcessDefinition) aProcessDefList.get(0);
                    Validations.check(aPosPD == null, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
                    reqData.setProcessDefinitionID(aPosPD.getIdentifier());
                    reqData.setOperationName(aPosPD.getProcessDefinitionName());
                }
            }
        }
        /*---------------------------------*/
        /*   Add Request Data into Queue   */
        /*---------------------------------*/
        messageDistributionManager.addMessageRequest(reqData);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strSystemMessageRequestForMultiFabIn
     * @return void
     * @exception
     * @author Ho
     * @date 2019/9/26 11:28
     */
    public void systemMessageRequestForMultiFab( Infos.ObjCommon strObjCommonIn,
            Infos.SystemMessageRequestForMultiFabIn strSystemMessageRequestForMultiFabIn){
        log.info("systemMessageRequestForMultiFab");

        List<Infos.MessageAttributesWithFabInfo> strMessageAttributesWithFabInfoSequence;
        strMessageAttributesWithFabInfoSequence = strSystemMessageRequestForMultiFabIn.getStrMessageListWithFabInfo();

        String currentFabID =  StandardProperties.OM_SITE_ID.getValue() ;
        log.info(""+ "currentFabID"+ currentFabID);

        int reqLen = CimArrayUtils.getSize(strMessageAttributesWithFabInfoSequence);
        log.info(""+ "request count for other fab"+ reqLen);

        for( int i=0; i<reqLen; i++ ){
            log.info(""+ "requested fabID"+ strMessageAttributesWithFabInfoSequence.get(i).getFabID());

            /* TODO: NOTIMPL
            SystemMessageXmlCreateOut strSystemMessageXmlCreateOut;
            SystemMessageXmlCreateIn  strSystemMessageXmlCreateIn;
            strSystemMessageXmlCreateIn.setStrMessageAttributesWithFabInfo ( strMessageAttributesWithFabInfoSequence.get(i));
            rc = systemMessageXmlCreate( strSystemMessageXmlCreateOut, strObjCommonIn,
                    strSystemMessageXmlCreateIn );
            char sarEvent.get(4001);

            sarEvent.get(0) = '\0';

            snprintf( sarEvent, sizeof(sarEvent), "|1|%s|%s|%s|%s|%s|%s",
                    (String)SP_INTERFAB_SAR_EVENT_COMPONENTNAME,
                    (String)SP_INTERFAB_SAR_EVENT_ACTION_START,
                    (String)currentFabID,
                    (String)strMessageAttributesWithFabInfoSequence.get(i).getFabID(),
                    (String)SP_INTERFAB_ACTIONTX_TXSYSTEMMSGRPT,
                    (String)strSystemMessageXmlCreateOut.getXml() );

            SarInterFabQueuePutDROut strSarInterFabQueuePutDROut;
            SarInterFabQueuePutDRIn  strSarInterFabQueuePutDRIn;
            strSarInterFabQueuePutDRIn.setSarEvent ( sarEvent);

            rc = sarInterFabQueuePutDR( strSarInterFabQueuePutDROut, strObjCommonIn,
                    strSarInterFabQueuePutDRIn );
            if( rc != RcOk ){
                log.info(""+ "sarInterFabQueuePutDR() != RcOk"+ rc);
                strSystemMessageRequestForMultiFabOut.setStrResult ( strSarInterFabQueuePutDROut.getStrResult());
                return rc;
            }*/
        }

        log.info("systemMessageRequestForMultiFab");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void messageQueuePut(Infos.ObjCommon objCommon, Inputs.MessageQueuePutIn messageQueuePutIn) {
        boolean writeFlag = false;
        //======================================================================================
        //   Check MessageQueue is needed or not.
        //======================================================================================
        boolean messageQueueFlag = false;
        String work = StandardProperties.OM_DELIVERY_MESSAGE_QUEUE_PUT_ENABLE.getValue();
        Validations.check(CimStringUtils.isEmpty(work), retCodeConfigEx.getNotFoundMsgQue());
        messageQueueFlag = Integer.parseInt(work) != 0;
        if (!messageQueueFlag){
            return;
        }
        //======================================================================================
        //    Check TXID for messageQue_Put or not
        //======================================================================================
        String hFIMSGQUETX_ID = objCommon.getTransactionID();
        String sql = String.format("SELECT COUNT(TRX_ID)\n" +
                "              FROM   OCNOTIFY\n" +
                "              WHERE  TRX_ID = '%s'", hFIMSGQUETX_ID);
        long totalcount = cimJpaRepository.count(sql);
        if (totalcount == 0){
            return;
        }
        //======================================================================================
        //   Set Initial Value to Host Value
        //======================================================================================
        int hFQMSGQUEITEM_FLAG = 0 ;
        boolean hFQMSGQUECAST_TRANS_RSV_FLG = false;
        boolean hFQMSGQUECAST_DISP_RSV_FLG  = false;
        String hFQMSGQUEEQP_ID = "";
        String hFQMSGQUEEQP_OBJ = "";
        String hFQMSGQUEEQP_MODE = "";
        String hFQMSGQUEEQPSTATE_ID = "";
        String hFQMSGQUEEQPSTATE_OBJ = "";
        String hFQMSGQUELOT_ID = "";
        String hFQMSGQUELOT_OBJ = "";
        String hFQMSGQUELOT_PROCESS_STATE = "";
        String hFQMSGQUELOT_HOLD_STATE = "";
        String hFQMSGQUECAST_ID = "";
        String hFQMSGQUECAST_OBJ ="";
        String hFQMSGQUECAST_TRANS_STATE = "";
        String hFQMSGQUEDRBL_ID = "";
        String hFQMSGQUEDRBL_OBJ = "";
        String hFQMSGQUEDRBL_TRANS_STATE = "";
        //======================================================================================
        //   Set Message Queue Records to Host Value
        //======================================================================================
        Timestamp hFQMSGQUEEVENT_TIME = objCommon.getTimeStamp().getReportTimeStamp();
        //======================================================================================
        //   Check OSEQAUTODISPATCHSET Table
        //======================================================================================
        String tmpUseCDRForAutoDispatchFlag = StandardProperties.OM_XFER_CARRIER_WITH_AUTO3_DISPATCH.getValue();
        boolean autoDispatchWriteFlag = true;
        if (CimStringUtils.equals(tmpUseCDRForAutoDispatchFlag, "1")){
            if (!ObjectIdentifier.isEmptyWithValue(messageQueuePutIn.getEquipmentID())){
                List<ObjectIdentifier> equipmentIDs = new ArrayList<>();
                equipmentIDs.add(messageQueuePutIn.getEquipmentID());
                List<Infos.EqpAuto3SettingInfo> eqpAuto3SettingInfoList = null;
                try {
                    eqpAuto3SettingInfoList = equipmentMethod.equipmentAuto3DispatchSettingListGetDR(objCommon, equipmentIDs);
                    boolean eventExistFlag = false;
                    int strEqpAuto3SettingInfoSeqlen = CimArrayUtils.getSize(eqpAuto3SettingInfoList);
                    for (int i = 0; i < strEqpAuto3SettingInfoSeqlen; i++){
                        Infos.EqpAuto3SettingInfo eqpAuto3SettingInfo = eqpAuto3SettingInfoList.get(i);
                        if (CimStringUtils.equals(eqpAuto3SettingInfo.getCarrierTransferRequestEvent(), BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_UNLOADREQ)
                                || CimStringUtils.equals(eqpAuto3SettingInfo.getCarrierTransferRequestEvent(), BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_LOADREQ)){
                            eventExistFlag = true;
                            break;
                        }
                    }
                    if (!eventExistFlag){
                        autoDispatchWriteFlag = false;
                    }
                } catch (ServiceException e) {
                    autoDispatchWriteFlag = false;
                }

            }
        }
        if (autoDispatchWriteFlag){
            //======================================================================================
            //   Reticle / Fixture Case
            //======================================================================================
            if (!ObjectIdentifier.isEmptyWithValue(messageQueuePutIn.getDurableID())){
                writeFlag = true;
                hFQMSGQUEDRBL_ID = messageQueuePutIn.getDurableID().getValue();
                hFQMSGQUEDRBL_OBJ = messageQueuePutIn.getDurableID().getReferenceKey();

                //------------------------------------
                //   Check Durable's transferState
                //------------------------------------
                if (!ObjectIdentifier.isEmptyWithValue(messageQueuePutIn.getEquipmentID())){
                    if (CimStringUtils.equals(messageQueuePutIn.getDurableTransferState(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                        hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_DURABLETRANSPORTSTATECHANGED;
                        hFQMSGQUEEQP_ID = messageQueuePutIn.getEquipmentID().getValue();
                        hFQMSGQUEEQP_OBJ = messageQueuePutIn.getEquipmentID().getReferenceKey();
                        hFQMSGQUEDRBL_TRANS_STATE = messageQueuePutIn.getDurableTransferState();
                    } else {
                        writeFlag = false;
                    }
                }
            }
            //======================================================================================
            //   Equipment Case
            //======================================================================================
            else if (!ObjectIdentifier.isEmptyWithValue(messageQueuePutIn.getEquipmentID())){
                writeFlag = true;
                hFQMSGQUEEQP_ID = messageQueuePutIn.getEquipmentID().getValue();
                hFQMSGQUEEQP_OBJ = messageQueuePutIn.getEquipmentID().getReferenceKey();
                //--------------------------------------
                //   Check Equipment's operationMode
                //--------------------------------------
                List<Infos.PortOperationMode> strPortOperationMode = messageQueuePutIn.getStrPortOperationMode();
                if (!CimArrayUtils.isEmpty(strPortOperationMode)){
                    int len = CimArrayUtils.getSize(strPortOperationMode);
                    for (int i = 0; i < len; i++){
                        Infos.PortOperationMode portOperationMode = strPortOperationMode.get(i);
                        if (!CimStringUtils.equals(portOperationMode.getOperationMode().getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)
                                || !CimStringUtils.equals(portOperationMode.getOperationMode().getDispatchMode(), BizConstant.SP_EQP_DISPATCHMODE_AUTO)) {
                            writeFlag = false;
                            break;
                        }
                    }
                    if (writeFlag){
                        hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_EQUIPMENTMODECHANGED;
                    }
                }
                //--------------------------------------
                //   Check machine type
                //--------------------------------------
                Boolean equipmentFlag = equipmentMethod.equipmentMachineTypeCheckDR(objCommon, messageQueuePutIn.getEquipmentID());
                if (!equipmentFlag){
                    writeFlag = false;
                }
            }
            //======================================================================================
            //   Cassette / Lot Case
            //======================================================================================
            else if (!ObjectIdentifier.isEmptyWithValue(messageQueuePutIn.getCassetteID()) || !ObjectIdentifier.isEmptyWithValue(messageQueuePutIn.getLotID())){
                writeFlag = true;
                //---------------------
                //   Cassette Case
                //---------------------
                if (!ObjectIdentifier.isEmptyWithValue(messageQueuePutIn.getCassetteID())){
                    hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_CASSETTEIDONLY;
                    hFQMSGQUECAST_ID = messageQueuePutIn.getCassetteID().getValue();
                    hFQMSGQUECAST_OBJ = messageQueuePutIn.getCassetteID().getReferenceKey();
                }
                //---------------------
                //   Lot Case
                //---------------------
                if (!ObjectIdentifier.isEmptyWithValue(messageQueuePutIn.getLotID())){
                    hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_LOTIDONLY;
                    hFQMSGQUELOT_ID = messageQueuePutIn.getLotID().getValue();
                    hFQMSGQUELOT_OBJ = messageQueuePutIn.getLotID().getReferenceKey();
                }
                //-------------------------------------
                //   Check Cassette's transferState
                //-------------------------------------
                if (!CimStringUtils.isEmpty(messageQueuePutIn.getCassetteTransferState())){
                    if (CimStringUtils.equals(messageQueuePutIn.getCassetteTransferState(), BizConstant.SP_TRANSSTATE_STATIONIN)
                            || CimStringUtils.equals(messageQueuePutIn.getCassetteTransferState(), BizConstant.SP_TRANSSTATE_BAYIN)
                            || CimStringUtils.equals(messageQueuePutIn.getCassetteTransferState(), BizConstant.SP_TRANSSTATE_MANUALIN)){
                        hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_CASSETTETRANSFERSTATECHANGED;
                        hFQMSGQUECAST_TRANS_STATE = messageQueuePutIn.getCassetteTransferState();
                    } else {
                        writeFlag = false;
                    }
                }
                //-------------------------------------------
                //   Check Cassette's transferReserveFlag
                //-------------------------------------------
                if (messageQueuePutIn.isCassetteTransferReserveFlag()){
                    writeFlag = false;
                } else {
                    hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_CASSETTETRANSFERRESERVECHANGED;
                    hFQMSGQUECAST_TRANS_RSV_FLG = messageQueuePutIn.isCassetteTransferReserveFlag();
                }
                //-------------------------------------
                //   Check Cassette's dispatchState
                //-------------------------------------
                if (messageQueuePutIn.isCassetteTransferReserveFlag()){
                    writeFlag = false;
                } else {
                    hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_CASSETTEDISPATCHSTATECHANGED;
                    hFQMSGQUECAST_DISP_RSV_FLG = messageQueuePutIn.isCassetteDispatchReserveFlag();
                }
                //-------------------------------
                //   Check Lot's processState
                //-------------------------------
                if (!CimStringUtils.isEmpty(messageQueuePutIn.getLotProcessState())){
                    if (CimStringUtils.equals(messageQueuePutIn.getLotProcessState(), BizConstant.SP_LOT_PROCSTATE_WAITING)){
                        hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_LOTPROCESSSTATECHANGED;
                        hFQMSGQUELOT_PROCESS_STATE = messageQueuePutIn.getLotProcessState();
                    } else {
                        writeFlag = false;
                    }
                }
                //-------------------------------
                //   Check Lot's holdState
                //-------------------------------
                if (!CimStringUtils.isEmpty(messageQueuePutIn.getLotHoldState())){
                    if (CimStringUtils.equals(messageQueuePutIn.getLotHoldState(), CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD)){
                        hFQMSGQUEITEM_FLAG = BizConstant.SP_WD_LOTHOLDSTATECHANGED;
                        hFQMSGQUELOT_HOLD_STATE = messageQueuePutIn.getLotHoldState();
                    } else {
                        writeFlag = false;
                    }
                }
            }
        }

        //======================================================================================
        //   Insert Message Queue Records
        //======================================================================================
        if (writeFlag){
            CimMessageQueueDO cimMessageQueueDO = new CimMessageQueueDO();
            cimMessageQueueDO.setEventTime(CimDateUtils.getCurrentTimeStamp());
            cimMessageQueueDO.setItemFlag(hFQMSGQUEITEM_FLAG);
            cimMessageQueueDO.setEquipmentID(hFQMSGQUEEQP_ID);
            cimMessageQueueDO.setEquipmentObj(hFQMSGQUEEQP_OBJ);
            cimMessageQueueDO.setEquipmentMode(hFQMSGQUEEQP_MODE);
            cimMessageQueueDO.setEquipmentStateID(hFQMSGQUEEQPSTATE_ID);
            cimMessageQueueDO.setEquipmentObj(hFQMSGQUEEQPSTATE_OBJ);
            cimMessageQueueDO.setLotID(hFQMSGQUELOT_ID);
            cimMessageQueueDO.setLotObj(hFQMSGQUELOT_OBJ);
            cimMessageQueueDO.setLotProcessState(hFQMSGQUELOT_PROCESS_STATE);
            cimMessageQueueDO.setLotHoldState(hFQMSGQUELOT_HOLD_STATE);
            cimMessageQueueDO.setCassetteID(hFQMSGQUECAST_ID);
            cimMessageQueueDO.setCassetteObj(hFQMSGQUECAST_OBJ);
            cimMessageQueueDO.setCassetteTransferState(hFQMSGQUECAST_TRANS_STATE);
            cimMessageQueueDO.setCassetteTransferReserveFlag(hFQMSGQUECAST_TRANS_RSV_FLG);
            cimMessageQueueDO.setCassetteDispatchReserveFlag(hFQMSGQUECAST_DISP_RSV_FLG);
            cimMessageQueueDO.setDurableID(hFQMSGQUEDRBL_ID);
            cimMessageQueueDO.setDurableObj(hFQMSGQUEDRBL_OBJ);
            cimMessageQueueDO.setDurableTransferState(hFQMSGQUEDRBL_TRANS_STATE);
            cimJpaRepository.save(cimMessageQueueDO);
        }
    }

    @Override
    public void slmMessageQueuePutDR(Infos.ObjCommon objCommon, Infos.StrSLMMsgQueueRecord strSLMMsgQueueRecord) {
        Validations.check(null == objCommon || null == strSLMMsgQueueRecord, retCodeConfig.getInvalidInputParam());
        ObjectIdentifier equipmentID = strSLMMsgQueueRecord.getEquipmentID();

        //--------------------------------------------------------------------------------------
        //  Check if SLMEvent put is required
        //     1. the equipmentID should be registered in auto3Setting by event of SLMRetrieve
        //-------------------------------------------------------------------------------------
        List<Infos.EqpAuto3SettingInfo> strEqpAuto3SettingInfoSeq = null;
        try {
            strEqpAuto3SettingInfoSeq = equipmentMethod.equipmentAuto3DispatchSettingListGetDR(objCommon, Collections.singletonList(equipmentID));
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getNotFoundEqpAuto3Setting())) {
                throw e;
            }
        }
        AtomicBoolean eventPutFlag = new AtomicBoolean(false);
        Optional.ofNullable(strEqpAuto3SettingInfoSeq).ifPresent(eqpAuto3SettingInfos -> {
            for (Infos.EqpAuto3SettingInfo eqpAuto3SettingInfo : eqpAuto3SettingInfos) {
                if (CimStringUtils.equals(eqpAuto3SettingInfo.getCarrierTransferRequestEvent(), BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_SLMRETRIEVE)) {
                    eventPutFlag.set(true);
                    break;
                }
            }
        });
        log.info("eventPutFlag : " + eventPutFlag.get());
        if (!eventPutFlag.get()) return;

        CimSLMMessageQueueDO messageQueueDO = new CimSLMMessageQueueDO();
        messageQueueDO.setEventTime(CimDateUtils.getCurrentTimeStamp());
        messageQueueDO.setEventName(strSLMMsgQueueRecord.getEventName());
        messageQueueDO.setEqpID(ObjectIdentifier.fetchValue(strSLMMsgQueueRecord.getEquipmentID()));
        messageQueueDO.setPortID(ObjectIdentifier.fetchValue(strSLMMsgQueueRecord.getPortID()));
        messageQueueDO.setCarrierID(ObjectIdentifier.fetchValue(strSLMMsgQueueRecord.getCassetteID()));
        messageQueueDO.setControlJobID(ObjectIdentifier.fetchValue(strSLMMsgQueueRecord.getControlJobID()));
        messageQueueDO.setLotID(ObjectIdentifier.fetchValue(strSLMMsgQueueRecord.getLotID()));
        messageQueueDO.setMessageID(strSLMMsgQueueRecord.getMessageID());
        cimJpaRepository.save(messageQueueDO);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void carrierOutMessageEventMake(Infos.ObjCommon objCommon,
                                           ObjectIdentifier carrierID,
                                           ObjectIdentifier equipmentID) {
        CimCarrierOutReqQueueDO entity = new CimCarrierOutReqQueueDO();
        entity.setCarrierID(ObjectIdentifier.fetchValue(carrierID));
        entity.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
        CimCarrierOutReqQueueDO event = cimJpaRepository.findOne(Example.of(entity)).orElse(null);
        if (null == event){
            if (log.isDebugEnabled()){
                log.debug("no exist event carrierID: {}, equipmentID: {}. make event",
                        ObjectIdentifier.fetchValue(carrierID),
                        ObjectIdentifier.fetchValue(equipmentID));
            }
            entity.setEventTime(CimDateUtils.getCurrentTimeStamp());
            cimJpaRepository.save(entity);
        }else {
            if (log.isDebugEnabled()){
                log.debug("exist event carrierID: {}, equipmentID: {}. skip event",
                        ObjectIdentifier.fetchValue(carrierID),
                        ObjectIdentifier.fetchValue(equipmentID));
            }
        }
    }
}
