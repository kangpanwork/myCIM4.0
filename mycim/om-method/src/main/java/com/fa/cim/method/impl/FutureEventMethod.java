package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.BaseStaticMethod;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IFutureEventMethod;
import com.fa.cim.newcore.bo.event.CimFutureSplitEvent;
import com.fa.cim.newcore.bo.event.EventManager;
import com.fa.cim.newcore.dto.event.Event;
import com.fa.cim.newcore.dto.event.FSMEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@OmMethod
@Slf4j
public class FutureEventMethod  implements IFutureEventMethod {
    @Autowired
    private EventManager eventManager;

    @Override
    public void experimentalLotExecEventMake(Infos.ObjCommon objCommon, String txId, String testMemo, com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailResultInfo strExperimentalLotDetailResultInfo) {
        //initial
        log.info("in para transactionID,{}", txId);
        log.info("in para testMemo,{}", testMemo);
        int i = 0;
        int j = 0;
        int lenDetail = 0;
        int lenWafer = 0;
        FSMEvent.FutureSplitEventRecord aEventRecord = new  FSMEvent.FutureSplitEventRecord();
        Event.EventData eventCommon = new Event.EventData();
        aEventRecord.setEventCommon(eventCommon);
        eventCommon.setTransactionID(txId);
        eventCommon.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventCommon.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventCommon.setUserID(objCommon.getUser().getUserID().getValue());
        eventCommon.setEventMemo(testMemo);

        //add psmJobIDfor hisory
        aEventRecord.setFsmJobID(strExperimentalLotDetailResultInfo.getFsmJobID());

        aEventRecord.setAction(strExperimentalLotDetailResultInfo.getAction());
        aEventRecord.setLotFamilyID(strExperimentalLotDetailResultInfo.getLotFamilyID().getValue());
        aEventRecord.setSplitRouteID(strExperimentalLotDetailResultInfo.getSplitOperationNumber());
        aEventRecord.setSplitOperationNumber(strExperimentalLotDetailResultInfo.getSplitOperationNumber());
        aEventRecord.setOriginalRouteID(strExperimentalLotDetailResultInfo.getOriginalRouteID().getValue());
        aEventRecord.setOriginalOperationNumber(strExperimentalLotDetailResultInfo.getOriginalOperationNumber());
        aEventRecord.setActionEMail(strExperimentalLotDetailResultInfo.isActionEMail());
        aEventRecord.setActionHold(strExperimentalLotDetailResultInfo.isActionHold());

        lenDetail = CimArrayUtils.getSize(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq());
        List<FSMEvent.FutureSplitRouteEventData> subRoutes = new ArrayList<>();
        aEventRecord.setSubRoutes(subRoutes);
        for (i = 0; i < lenDetail; i++) {
            FSMEvent.FutureSplitRouteEventData splitSubRouteEventData = new FSMEvent.FutureSplitRouteEventData();
            subRoutes.add(splitSubRouteEventData);
            splitSubRouteEventData.setRouteID(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getRouteID().getValue());
            splitSubRouteEventData.setReturnOperationNumber(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getReturnOperationNumber());
            splitSubRouteEventData.setMergeOperationNumber(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getMergeOperationNumber());
            splitSubRouteEventData.setParentLotID(ObjectIdentifier.fetchValue(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getParentLotID()));
            splitSubRouteEventData.setChildLotID(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getChildLotID() == null ? null : strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getChildLotID().getValue());
            splitSubRouteEventData.setMemo(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getMemo());

            lenWafer = CimArrayUtils.getSize(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getStrExperimentalLotWaferSeq());
            List<FSMEvent.FutureSplitedWaferEventData> wafers = new ArrayList<>();
            splitSubRouteEventData.setWafers(wafers);

            for (j = 0; j < lenWafer; j++) {
                FSMEvent.FutureSplitedWaferEventData splitedWaferEventData = new FSMEvent.FutureSplitedWaferEventData();
                splitedWaferEventData.setWaferID(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getStrExperimentalLotWaferSeq().get(j).getWaferId().getWaferID().getValue());
                splitedWaferEventData.setSuccessFlag(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getStrExperimentalLotWaferSeq().get(j).getStatus());
                splitedWaferEventData.setGroupNo(strExperimentalLotDetailResultInfo.getStrExperimentalFutureLotDetailSeq().get(i).getStrExperimentalLotWaferSeq().get(j).getGroupNo());
                wafers.add(splitedWaferEventData);
            }
        }
        //Put into the queue of PosEventManager
        //Create FutureReworkRequest event
        eventManager.createEvent(aEventRecord, CimFutureSplitEvent.class);
    }

    @Override
    public void experimentalLotRegistEventMake(Infos.ObjCommon objCommon, String transactionID, String testMemo, com.fa.cim.fsm.Infos.ExperimentalFutureLotRegistInfo experimentalLotRegistInfo) {
        FSMEvent.FutureSplitEventRecord aEventRecord = new FSMEvent.FutureSplitEventRecord ();
        aEventRecord.setEventCommon(setEventData(objCommon, transactionID, testMemo));
        //add psmJobID for history
        aEventRecord.setFsmJobID(experimentalLotRegistInfo.getFsmJobID());
        aEventRecord.setAction(experimentalLotRegistInfo.getAction());
        aEventRecord.setLotFamilyID(ObjectIdentifier.fetchValue(experimentalLotRegistInfo.getLotFamilyID()));
        aEventRecord.setSplitRouteID(ObjectIdentifier.fetchValue(experimentalLotRegistInfo.getSplitRouteID()));
        aEventRecord.setSplitOperationNumber(experimentalLotRegistInfo.getSplitOperationNumber());
        aEventRecord.setOriginalRouteID(ObjectIdentifier.fetchValue(experimentalLotRegistInfo.getOriginalRouteID()));
        aEventRecord.setOriginalOperationNumber(experimentalLotRegistInfo.getOriginalOperationNumber());
        aEventRecord.setActionEMail(experimentalLotRegistInfo.isActionEMail());
        aEventRecord.setActionHold(experimentalLotRegistInfo.isActionHold());
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist> experimentalLotRegistList = experimentalLotRegistInfo.getStrExperimentalFutureLotRegistSeq();
        List<FSMEvent.FutureSplitRouteEventData> subRoutes = new ArrayList<>();
        aEventRecord.setSubRoutes(subRoutes);
        for (com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist experimentalLotRegist : experimentalLotRegistList) {
            FSMEvent.FutureSplitRouteEventData splitSubRouteEventData = new FSMEvent.FutureSplitRouteEventData ();
            subRoutes.add(splitSubRouteEventData);
            splitSubRouteEventData.setRouteID(ObjectIdentifier.fetchValue(experimentalLotRegist.getRouteID()));
            splitSubRouteEventData.setReturnOperationNumber(experimentalLotRegist.getReturnOperationNumber());
            splitSubRouteEventData.setMergeOperationNumber(experimentalLotRegist.getMergeOperationNumber());
            splitSubRouteEventData.setParentLotID("");
            splitSubRouteEventData.setChildLotID("");
            splitSubRouteEventData.setMemo(experimentalLotRegist.getMemo());
            List<FSMEvent.FutureSplitedWaferEventData> wafers = new ArrayList<>();
            splitSubRouteEventData.setWafers(wafers);
            List<com.fa.cim.fsm.Infos.Wafer> waferIDs = experimentalLotRegist.getWaferIDs();
            for (com.fa.cim.fsm.Infos.Wafer waferID : waferIDs) {
                FSMEvent.FutureSplitedWaferEventData splitedWaferEventData = new FSMEvent.FutureSplitedWaferEventData();
                splitedWaferEventData.setWaferID(ObjectIdentifier.fetchValue(waferID.getWaferID()));
                splitedWaferEventData.setGroupNo(waferID.getGroupNo());
                splitedWaferEventData.setSuccessFlag("");
                wafers.add(splitedWaferEventData);
            }
        }
        //------------------------------------------------------------------------//
        // Put into the queue of PosEventManager                                  //
        //------------------------------------------------------------------------//
        eventManager.createEvent(aEventRecord, CimFutureSplitEvent.class);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2019/11/6 12:48
     * @param objCommon
     * @param claimMemo -
     * @return com.fa.cim.newcore.dto.event.Event.EventData
     */
    private Event.EventData setEventData(Infos.ObjCommon objCommon, String transactionID, String claimMemo) {
        Event.EventData eventData = new Event.EventData();
        eventData.setTransactionID(transactionID);
        eventData.setEventTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        eventData.setEventShopDate(objCommon.getTimeStamp().getReportShopDate());
        eventData.setUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        eventData.setEventMemo(claimMemo);
        eventData.setEventCreationTimeStamp(CimDateUtils.getCurrentTimeStamp().toString());
        return eventData;
    }
}
