package com.fa.cim.service.probe.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.IFixtureMethod;
import com.fa.cim.method.IStockerMethod;
import com.fa.cim.method.impl.DurableMethod;
import com.fa.cim.service.equipment.IEquipmentInqService;
import com.fa.cim.service.probe.IProbeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @exception
 * @author ho
 * @date 2020/11/11 17:54
 */
@OmService
@Slf4j
public class ProbeServiceImpl implements IProbeService {

    @Autowired
    private IFixtureMethod fixtureMethod;

    @Autowired
    private DurableMethod durableMethod;

    @Autowired
    private IStockerMethod iStockerMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IStockerMethod stockerMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentInqService equipmentInqService;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Override
    public Results.FixtureStatusChangeRptResult sxFixtureStatusChangeRpt(Infos.ObjCommon strObjCommonIn,
                                                                         ObjectIdentifier fixtureID,
                                                                         String fixtureStatus,
                                                                         String claimMemo) {
        Results.FixtureStatusChangeRptResult fixtureStatusChangeRptResult = new Results.FixtureStatusChangeRptResult();
        Results.FixtureStatusInqResult fixtureStatusInqResult = new Results.FixtureStatusInqResult();


        /*-----------------------------------------------*/
        /*   Get and Check fixture transfer status 0.01  */
        /*-----------------------------------------------*/
        fixtureStatusInqResult= fixtureMethod.fixtureFillInTxPDQ002DR(strObjCommonIn, fixtureID);

        /*---------------------------*/
        /*   Change fixture status   */
        /*---------------------------*/
       fixtureMethod.fixtureStateChange(strObjCommonIn, fixtureID, fixtureStatus);
        /*-----------------------*/
        /*   Set out structure   */
        /*-----------------------*/
        fixtureStatusChangeRptResult= fixtureMethod.fixtureFillInTxPDR001(strObjCommonIn, fixtureID);


        /*---------------------------------*/
        /* Create Durable Change Event     */
        /*---------------------------------*/
        eventMethod.durableChangeEventMake(strObjCommonIn, TransactionIDEnum.PROEB_STATUS_CHANGE_RPT.getValue(), fixtureID, BizConstant.SP_DURABLECAT_FIXTURE, BizConstant.SP_DURABLEEVENT_ACTION_STATECHANGE, claimMemo);
        return fixtureStatusChangeRptResult;
    }

    @Override
    public ObjectIdentifier sxProbeUsageCountResetReq(Infos.ObjCommon strObjCommonIn,
                                                      ObjectIdentifier fixtureID,
                                                      String claimMemo) {
        /*-------------------------------------*/
        /*   Reset fixture usage information   */
        /*-------------------------------------*/

        fixtureMethod.fixtureUsageInfoReset(strObjCommonIn,fixtureID);
        /*---------------------------------------------*/
        /*   D4100081  Create Durable Change Event     */
        /*---------------------------------------------*/
        eventMethod.durableChangeEventMake(
                strObjCommonIn,
                "TXPDC003",
                fixtureID,
                BizConstant.SP_DURABLECAT_FIXTURE,
                BizConstant.SP_DURABLEEVENT_ACTION_PMRESET,
                claimMemo);

        /*-----------------------*/
        /*   Set out structure   */
        /*-----------------------*/
        return( fixtureID );
    }

    @Override
    public void sxFixtureStatusMultiChangeRpt(Infos.ObjCommon strObjCommonIn, String fixtureStatus,
                                              List<ObjectIdentifier> fixtureID, String claimMemo) {
        int nLen = fixtureID.size();
        /*---------------------------*/
        /*   Status Change Request   */
        /*---------------------------*/
        for (int i=0 ; i<nLen ; i++ ) {
            Results.FixtureStatusChangeRptResult fixtureStatusChangeRptResult;
            fixtureStatusChangeRptResult = sxFixtureStatusChangeRpt(
                                           strObjCommonIn,
                                           fixtureID.get(i),
                                           fixtureStatus,
                                           claimMemo );
        }
    }

    @Override
    public Results.FixtureXferStatusChangeRptResult  sxFixtureXferStatusChangeRpt(
            Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID, ObjectIdentifier equipmentID,
            List<Infos.XferFixture> strXferFixture) {

        Results.FixtureXferStatusChangeRptResult fixtureXferStatusChangeRptResult = new Results.FixtureXferStatusChangeRptResult();
       // long i,nLen = strXferFixture.length() ;
        int i = strXferFixture.size(),nLen = strXferFixture.size();
        /*==============================================*/
        /* Departure or Destination should be one place */
        /*==============================================*/
        Validations.check(!ObjectUtils.isEmpty(stockerID)  && !ObjectUtils.isEmpty(equipmentID), "Departure or Destination should be one place...");

        /*==============================================*/
        /* Data combination                             */
        /*==============================================*/
        if(!ObjectUtils.isEmpty(stockerID))
        {
            Outputs.ObjStockerTypeGetDROut strStockerTypeGetDROut = stockerMethod.stockerTypeGetDR(strObjCommonIn, stockerID);

            /*-*-*-<Dept./Dest. STOCKER CASE>*-*-*-*-*-*-*-*-*-*-*/
            int aSwitchNumber ;
            if(CimStringUtils.equals(strStockerTypeGetDROut.getStockerType(),BizConstant.SP_STOCKER_TYPE_FIXTURE)){
                aSwitchNumber = 0 ;
            }else {
                aSwitchNumber = 1 ;
            }

            switch (aSwitchNumber)
            {
                case 0 :
                {
                    for(i=0; i<nLen ; i++)
                    {
                        if(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(), BizConstant.SP_TRANSSTATE_SHELFIN) || CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(), BizConstant.SP_TRANSSTATE_SHELFOUT)){
                           continue ;
                        }else{
                            Validations.check(true, retCodeConfig.getInvalidDataCombinAtion(),"*****",stockerID.getValue());//need confirm msg
                        }
                    }
                }
                    break;
                    default :
                    Validations.check(true, retCodeConfig.getInvalidStockerType(), "*****",
                                   stockerID.getValue());
            }
        }
        /*-*-*-<Dept./Dest. EQUIPMENT CASE>*-*-*-*-*-*-*-*-*-*/
        if(!ObjectUtils.isEmpty(equipmentID)){
            Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
            eqpInfoInqParams.setEquipmentID(equipmentID);
            eqpInfoInqParams.setRequestFlagForBRInfo(true);//Boolean setRequestFlagForBRInfo = true;
            eqpInfoInqParams.setRequestFlagForStatusInfo(true);//boolean requestFlagForStatusInfo = true;
            eqpInfoInqParams.setRequestFlagForPMInfo(false);//boolean requestFlagForPMInfo = false;
            eqpInfoInqParams.setRequestFlagForPMInfo(false);//boolean requestFlagForPortInfo = false;
            eqpInfoInqParams.setRequestFlagForChamberInfo(false);//boolean requestFlagForChamberInfo = false;
            eqpInfoInqParams.setRequestFlagForStockerInfo(false);//boolean requestFlagForStockerInfo = false;
            eqpInfoInqParams.setRequestFlagForInprocessingLotInfo(false);// boolean requestFlagForInprocessingLotInfo = false;
            eqpInfoInqParams.setRequestFlagForReservedControlJobInfo(false);// boolean requestFlagForReservedControlJobInfo = false;
            eqpInfoInqParams.setRequestFlagForRSPPortInfo(false); //boolean requestFlagForRSPPortInfo = false;
            eqpInfoInqParams.setRequestFlagForEqpContainerInfo(false);//boolean requestFlagForEqpContainerInfo = false;
            Results.EqpInfoInqResult eqpInfoInqResult = equipmentInqService.sxEqpInfoInq(strObjCommonIn,eqpInfoInqParams);

            for(i=0 ;i<nLen ; i++)
            {
                if(!(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(),BizConstant.SP_TRANSSTATE_EQUIPMENTIN) || CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(),BizConstant.SP_TRANSSTATE_EQUIPMENTOUT))){
                    Validations.check(true,retCodeConfig.getInvalidDataCombinAtion(),"******");
                }

                if(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(),BizConstant.SP_TRANSSTATE_EQUIPMENTIN) && eqpInfoInqResult.getEquipmentBRInfo().isFixtureUseFlag()){
                    Validations.check(true,retCodeConfig.getEquipmentNotRequiredFixture(),"******");
                }

            }

        }
        List<String> preTransferStatus = new ArrayList<>();
        for(i=0 ;i<nLen ; i++) {
            Results.FixtureStatusInqResult fixtureFillInTxPDQ002DR = fixtureMethod.fixtureFillInTxPDQ002DR(strObjCommonIn,strXferFixture.get(i).getFixtureID()) ;
            preTransferStatus.add(fixtureFillInTxPDQ002DR.getFixtureStatusInfo().getTransferStatus());
        }


        /*-----------------------------------------------------------*/
        /* All check has been done, then                             */
        /* Change Fixture's transfer status                          */
        /*-----------------------------------------------------------*/
        Outputs.ObjFixtureChangeTransportStateOut objFixtureChangeTransportStateOut = fixtureMethod.fixtureChangeTransportState(strObjCommonIn, stockerID, equipmentID, strXferFixture);
        fixtureXferStatusChangeRptResult.setStockerID(objFixtureChangeTransportStateOut.getStockerID());
        fixtureXferStatusChangeRptResult.setEquipmentID(objFixtureChangeTransportStateOut.getEquipmentID());
        fixtureXferStatusChangeRptResult.setStrXferFixture(objFixtureChangeTransportStateOut.getStrXferFixture());

        /*-----------------------------------------------------------*/
        /* Change Fixture's status                              0.01 */
        /*-----------------------------------------------------------*/
        String recordTimeStamp;
        for(i=0 ;i<nLen ; i++)
        {
            /*---------------------------------------------*/
            /* D4100110  Create Durable Xfer Change Event  */
            /*---------------------------------------------*/
            ObjectIdentifier machineID = new ObjectIdentifier();
            if(!ObjectIdentifier.isEmpty(stockerID)){
                machineID = stockerID;
            }if(!ObjectIdentifier.isEmpty(equipmentID)){
                machineID = equipmentID;
            }

            if(CimObjectUtils.isEmpty(strXferFixture.get(i).getTransferStatusChangeTimeStamp())){
                recordTimeStamp = CimDateUtils.getTimestampAsString(strObjCommonIn.getTimeStamp().getReportTimeStamp());    //P4100317
            }else{
                recordTimeStamp = strXferFixture.get(i).getTransferStatusChangeTimeStamp();
            }
            Inputs.DurableXferStatusChangeEventMakeParams durableXferStatusChangeEventMakeParams = new Inputs.DurableXferStatusChangeEventMakeParams();
            durableXferStatusChangeEventMakeParams.setTransactionID("TXPDR003");
            durableXferStatusChangeEventMakeParams.setDurableID(strXferFixture.get(i).getFixtureID());
            durableXferStatusChangeEventMakeParams.setDurableType(BizConstant.SP_DURABLECAT_FIXTURE);
            durableXferStatusChangeEventMakeParams.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
            durableXferStatusChangeEventMakeParams.setDurableStatus("");
            durableXferStatusChangeEventMakeParams.setXferStatus(strXferFixture.get(i).getTransferStatus());
            durableXferStatusChangeEventMakeParams.setTransferStatusChangeTimeStamp(recordTimeStamp);
            durableXferStatusChangeEventMakeParams.setLocation(machineID.getValue());
            durableXferStatusChangeEventMakeParams.setClaimMemo("");
            eventMethod.durableXferStatusChangeEventMake(strObjCommonIn, durableXferStatusChangeEventMakeParams);


            if(CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(),BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                fixtureMethod.fixtureStateChange(strObjCommonIn,strXferFixture.get(i).getFixtureID(),BizConstant.CIMFW_DURABLE_INUSE);
                /*---------------------------------------------*/
                /*   D4100081  Create Durable Change Event     */
                /*---------------------------------------------*/
                Inputs.DurableXferStatusChangeEventMakeParams strDurableChangeEventMakeOut = new Inputs.DurableXferStatusChangeEventMakeParams();
                strDurableChangeEventMakeOut.setTransactionID("TXPDR003");
                strDurableChangeEventMakeOut.setDurableID(strXferFixture.get(i).getFixtureID());
                strDurableChangeEventMakeOut.setDurableType(BizConstant.SP_DURABLECAT_FIXTURE);
                strDurableChangeEventMakeOut.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
                strDurableChangeEventMakeOut.setDurableStatus("");
                eventMethod.durableXferStatusChangeEventMake(strObjCommonIn, durableXferStatusChangeEventMakeParams);
            } else if(CimStringUtils.equals(preTransferStatus.get(i),BizConstant.SP_TRANSSTATE_EQUIPMENTIN) &&
                        CimStringUtils.equals(strXferFixture.get(i).getTransferStatus(),BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                fixtureMethod.fixtureStateChange(strObjCommonIn,strXferFixture.get(i).getFixtureID(),BizConstant.CIMFW_DURABLE_AVAILABLE);
                /*---------------------------------------------*/
                /*   D4100081  Create Durable Change Event     */
                /*---------------------------------------------*/
                Inputs.DurableXferStatusChangeEventMakeParams strDurableChangeEventMakeOut = new Inputs.DurableXferStatusChangeEventMakeParams();
                strDurableChangeEventMakeOut.setTransactionID("TXPDR003");
                strDurableChangeEventMakeOut.setDurableID(strXferFixture.get(i).getFixtureID());
                strDurableChangeEventMakeOut.setDurableType(BizConstant.SP_DURABLECAT_FIXTURE);
                strDurableChangeEventMakeOut.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_STATECHANGE);
                strDurableChangeEventMakeOut.setDurableStatus("");
                eventMethod.durableXferStatusChangeEventMake(strObjCommonIn, durableXferStatusChangeEventMakeParams);
            }

        }
        return  fixtureXferStatusChangeRptResult;
    }
}
