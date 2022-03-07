package com.fa.cim.controller.probe;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.probe.IProbeController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.probe.IProbeService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/5          ********            Jerry_Huang                create file
 *
 * @author Jerry_Huang
 * @since 2020/11/5 10:17
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IProbeController.class, confirmableKey = "ProbeConfirm", cancellableKey = "ProbeCancel")
@RequestMapping("/probe")
@Listenable
public class ProbeController implements IProbeController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IProbeService probeService;

    @ResponseBody
    @RequestMapping(value = "/probe_status_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROEB_STATUS_CHANGE_RPT)
    @Override
    public Response ProbeStatusChangeRpt(@RequestBody Params.ProbeStatusChangeParams probeStatusChangeParams) {
        // init params
        final String transactionID = TransactionIDEnum.PROEB_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/


        ObjectIdentifier retVal = new ObjectIdentifier();
        Infos.ObjCommon strObjCommonIn ;
        Params.AccessControlCheckInqParams accessControlCheckInqParams=new Params.AccessControlCheckInqParams();
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,probeStatusChangeParams.getUser(),accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.FixtureStatusChangeRptResult fixtureStatusChangeRptResult = probeService.sxFixtureStatusChangeRpt(strObjCommonIn, probeStatusChangeParams.getFixtureID(), probeStatusChangeParams.getFixtureStatus(),probeStatusChangeParams.getClaimMemo()); ;

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/

        return Response.createSucc(transactionID, fixtureStatusChangeRptResult);
    }

    @ResponseBody
    @RequestMapping(value = "/probe_usage_count_reset/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROBE_USAGE_COUNT_RESET_REQ)
    @Override
    public Response probeUsageCountResetReq(@RequestBody Params.ProbeUsageCountResetReqParams probeUsageCountResetReqParams) {
        final String transactionID = TransactionIDEnum.PROBE_USAGE_COUNT_RESET_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/

        ObjectIdentifier retVal = new ObjectIdentifier();

        Infos.ObjCommon strObjCommonIn ;

        Params.AccessControlCheckInqParams accessControlCheckInqParams=new Params.AccessControlCheckInqParams();
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,probeUsageCountResetReqParams.getUser(),accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        retVal = probeService.sxProbeUsageCountResetReq( strObjCommonIn, probeUsageCountResetReqParams.getFixtureID(), probeUsageCountResetReqParams.getClaimMemo()) ;

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(retVal) ;
    }

    @ResponseBody
    @RequestMapping(value = "/probe_status_multi_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROBE_STATUS_MULTI_CHANGE_RPT)
    @Override
    public Response probeStatusMultiChangeRpt(@RequestBody Params.ProbeStatusMultiChangeRptParams probeStatusMultiChangeRptParams){
        final String transactionID = TransactionIDEnum.PROBE_STATUS_MULTI_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/

        ObjectIdentifier retVal = new ObjectIdentifier();
        Infos.ObjCommon strObjCommonIn ;

        Params.AccessControlCheckInqParams accessControlCheckInqParams=new Params.AccessControlCheckInqParams();
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,probeStatusMultiChangeRptParams.getUser(),accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        probeService.sxFixtureStatusMultiChangeRpt(strObjCommonIn, probeStatusMultiChangeRptParams.getFixtureStatus(), probeStatusMultiChangeRptParams.getFixtureID(),probeStatusMultiChangeRptParams.getClaimMemo());
        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, "");
    }

    @ResponseBody
    @RequestMapping(value = "/probe_xfer_status_change/rpt", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROBE_XFER_STATUS_CHANGE_RPT)
    @Override
    public Response probeXferStatusChangeRpt(@RequestBody Params.probeXferStatusChangeRptParams probeXferStatusChangeRptParams) {
        // init params
        final String transactionID = TransactionIDEnum.PROBE_XFER_STATUS_CHANGE_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/

        ObjectIdentifier retVal = new ObjectIdentifier();

        Infos.ObjCommon strObjCommonIn ;

        // Initialising strObjCommonIn's first two parameters
        Params.AccessControlCheckInqParams accessControlCheckInqParams=new Params.AccessControlCheckInqParams();
        strObjCommonIn = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,probeXferStatusChangeRptParams.getUser(),accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.FixtureXferStatusChangeRptResult fixtureXferStatusChangeRptResult = probeService.sxFixtureXferStatusChangeRpt(strObjCommonIn,probeXferStatusChangeRptParams.getStockerID(),probeXferStatusChangeRptParams.getEquipmentID(),probeXferStatusChangeRptParams.getStrXferFixture());
        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, fixtureXferStatusChangeRptResult);
    }
}
