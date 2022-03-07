package com.fa.cim.controller.bond;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.bond.IBondController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.*;
import com.fa.cim.method.IBondingGroupMethod;
import com.fa.cim.method.IBondingMapMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.bond.*;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * <p>BondingController .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/16 10:23         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/16 10:23
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IBondController.class, confirmableKey = "BondConfirm", cancellableKey = "BondCancel")
@Listenable
@RestController
@RequestMapping("/bond")
public class BondController implements IBondController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private IBondingMapMethod bondingMapMethod;

    @Autowired
    private IPostService postService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IBondService bondService;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private PostController postController;

    @ResponseBody
    @PostMapping(value = "/bonding_group_modify/req")
    @Override
    @CimMapping(TransactionIDEnum.BONDING_GROUP_MODIFY_REQ)
    public Response bondingGroupModifyReq(@RequestBody Params.BondingGroupUpdateReqInParams bondingGroupUpdateReqInParams) {
        Validations.check(null == bondingGroupUpdateReqInParams, retCodeConfig.getInvalidParameter());
        String transactionID = TransactionIDEnum.BONDING_GROUP_MODIFY_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //-------------------------------------------------------------------------
        //
        //   Pre Process
        //
        //-------------------------------------------------------------------------
        Results.BondingGroupUpdateReqResult retVal;
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, bondingGroupUpdateReqInParams.getUser());

        //------------------------------
        // Set Event Parameter
        //------------------------------
        List<Infos.EventParameter> eventParameters = new ArrayList<>();
        String updateMode = bondingGroupUpdateReqInParams.getUpdateMode();
        List<Infos.BondingGroupInfo> bondingGroupInfoSeq = bondingGroupUpdateReqInParams.getStrBondingGroupInfoSeq();
        if (CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_UPDATE)
                || CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_DELETE)) {
            Optional.ofNullable(bondingGroupInfoSeq).ifPresent(bondingGroupInfo -> {
                StringBuilder strBondGrpID = new StringBuilder();
                Iterator<Infos.BondingGroupInfo> iterator = bondingGroupInfo.iterator();
                while (iterator.hasNext()) {
                    strBondGrpID.append(iterator.next().getBondingGroupID());
                    if (iterator.hasNext()) {
                        strBondGrpID.append(",");
                    }
                }
                Infos.EventParameter bondGroupEvent = new Infos.EventParameter();
                bondGroupEvent.setParameterName("BOND_GRP_ID");
                bondGroupEvent.setParameterValue(strBondGrpID.toString());
                eventParameters.add(bondGroupEvent);
            });
        }

        if (CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_UPDATE)
                || CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_CREATE)) {
            Optional.ofNullable(bondingGroupInfoSeq).ifPresent(bondingGroupInfo -> {
                StringBuilder equipmentID = new StringBuilder();
                Iterator<Infos.BondingGroupInfo> iterator = bondingGroupInfo.iterator();
                while (iterator.hasNext()) {
                    equipmentID.append(ObjectIdentifier.fetchValue(iterator.next().getTargetEquipmentID()));
                    if (iterator.hasNext()) {
                        equipmentID.append(",");
                    }
                }
                Infos.EventParameter bondEQPEvent = new Infos.EventParameter();
                bondEQPEvent.setParameterName("EQP_ID");
                bondEQPEvent.setParameterValue(equipmentID.toString());
                eventParameters.add(bondEQPEvent);
            });
        }

        if (CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_CREATE)) {
            Optional.ofNullable(bondingGroupInfoSeq).ifPresent(bondingGroupInfo -> {
                StringBuilder baseLotID = new StringBuilder();
                StringBuilder baseProductID = new StringBuilder();
                StringBuilder topLotID = new StringBuilder();
                StringBuilder topProductID = new StringBuilder();
                for (Infos.BondingGroupInfo groupInfo : bondingGroupInfo) {
                    Optional.ofNullable(groupInfo.getBondingMapInfoList()).ifPresent(data -> {
                        Iterator<Infos.BondingMapInfo> bondingMapInfo = data.iterator();
                        if (bondingMapInfo.hasNext()) {
                            Infos.BondingMapInfo mapInfo = bondingMapInfo.next();
                            baseLotID.append(ObjectIdentifier.fetchValue(mapInfo.getBaseLotID()));
                            baseProductID.append(ObjectIdentifier.fetchValue(mapInfo.getBaseProductID()));
                            topLotID.append(ObjectIdentifier.fetchValue(mapInfo.getPlanTopLotID()));
                            topProductID.append(ObjectIdentifier.fetchValue(mapInfo.getPlanTopProductID()));
                            if (bondingMapInfo.hasNext()) {
                                baseLotID.append(",");
                                baseProductID.append(",");
                                topLotID.append(",");
                                topProductID.append(",");
                            }
                        }
                    });
                }
                Infos.EventParameter baseLotEvent = new Infos.EventParameter();
                baseLotEvent.setParameterName("BASE_LOT_ID");
                baseLotEvent.setParameterValue(baseLotID.toString());
                eventParameters.add(baseLotEvent);

                Infos.EventParameter baseProductEvent = new Infos.EventParameter();
                baseProductEvent.setParameterName("BASE_PROD_ID");
                baseProductEvent.setParameterValue(baseProductID.toString());
                eventParameters.add(baseProductEvent);

                Infos.EventParameter topLotEvent = new Infos.EventParameter();
                topLotEvent.setParameterName("TOP_LOT_ID");
                topLotEvent.setParameterValue(topLotID.toString());
                eventParameters.add(topLotEvent);

                Infos.EventParameter topProductEvent = new Infos.EventParameter();
                topProductEvent.setParameterName("TOP_PROD_ID");
                topProductEvent.setParameterValue(topProductID.toString());
                eventParameters.add(topProductEvent);
            });
        }

        eventParameters.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));

        //------------------------------
        // Privilege Check
        //------------------------------
        List<ObjectIdentifier> lotIDSeq = new ArrayList<>();
        assert bondingGroupInfoSeq != null;
        Outputs.ObjBondingGroupInfoGetDROut objBondingGroupInfoGetDROut = null;
        for (Infos.BondingGroupInfo bondingGroupInfo : bondingGroupInfoSeq) {
            if (CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_UPDATE)
                    || CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_DELETE)) {
                //------------------------------
                // bondingGroup_info_GetDR
                //------------------------------
                objBondingGroupInfoGetDROut = bondingGroupMethod.bondingGroupInfoGetDR(objCommon, bondingGroupInfo.getBondingGroupID(), true);

            } else if (CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_CREATE)) {
                Set<ObjectIdentifier> bondingLotIDList = new HashSet<>();
                List<Infos.BondingMapInfo> bondingMapInfoList = bondingGroupInfo.getBondingMapInfoList();
                if (CimArrayUtils.isNotEmpty(bondingMapInfoList)) {
                    for (Infos.BondingMapInfo bondingMapInfo : bondingMapInfoList) {
                        bondingLotIDList.add(bondingMapInfo.getBaseLotID());
                        bondingLotIDList.add(bondingMapInfo.getPlanTopLotID());
                    }
                }
                objBondingGroupInfoGetDROut = new Outputs.ObjBondingGroupInfoGetDROut();
                objBondingGroupInfoGetDROut.setBondingLotIDList(new ArrayList<>(bondingLotIDList));
            }

            assert objBondingGroupInfoGetDROut != null;
            if (CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_CREATE)
                    || CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_UPDATE)) {
                Infos.BondingGroupInfo bondingGroup = objBondingGroupInfoGetDROut.getBondingGroupInfo();
                if (null == bondingGroup) {
                    bondingGroup = new Infos.BondingGroupInfo();
                }
                bondingGroup.setTargetEquipmentID(bondingGroupInfo.getTargetEquipmentID());
            }

            //------------------------------
            // txPrivilegeCheckReq
            //------------------------------
            Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams();
            checkInqParams.setEquipmentID(ObjectIdentifier.emptyIdentifier());
            checkInqParams.setLotIDLists(objBondingGroupInfoGetDROut.getBondingLotIDList());
            accessInqService.sxAccessControlCheckInq(objCommon, checkInqParams);

            lotIDSeq.addAll(objBondingGroupInfoGetDROut.getBondingLotIDList());
        }


        //-------------------------------------------------------------------------
        //
        //   Main Process
        //
        //-------------------------------------------------------------------------
        //------------------------------
        // BondingGroupModifyReq
        //------------------------------
        retVal = bondService.sxBondingGroupUpdateReq(objCommon, bondingGroupUpdateReqInParams);

        //------------------------------
        // Reset out event parameter
        //------------------------------
        if (CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_CREATE)) {
            eventParameters.clear();
            Optional.ofNullable(retVal).ifPresent(result -> {
                StringBuilder bondGroupID = new StringBuilder();
                Iterator<Infos.BondingGroupUpdateResult> iterator = result.getStrBondingGroupUpdateResultSeq().iterator();
                while (iterator.hasNext()) {
                    bondGroupID.append(iterator.next().getBondingGroupID());
                    if (iterator.hasNext()) {
                        bondGroupID.append(",");
                    }
                }
                Infos.EventParameter bondEQPEvent = new Infos.EventParameter();
                bondEQPEvent.setParameterName("BOND_GRP_ID");
                bondEQPEvent.setParameterValue(bondGroupID.toString());
                eventParameters.add(bondEQPEvent);
            });
            eventParameters.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));
        }

        //-----------------------------
        // Resister post action.
        //-----------------------------
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
        postTaskRegisterReqParams.setTransactionID(ThreadContextHolder.getTransactionId());
        postTaskRegisterReqParams.setPatternID(null);
        postTaskRegisterReqParams.setKey(null);
        postTaskRegisterReqParams.setSequenceNumber(-1);
        postTaskRegisterReqParams.setUser(bondingGroupUpdateReqInParams.getUser());
        postTaskRegisterReqParams.setClaimMemo(bondingGroupUpdateReqInParams.getClaimMemo());

        Infos.PostProcessRegistrationParam registrationParam = new Infos.PostProcessRegistrationParam();
        registrationParam.setLotIDs(lotIDSeq);
        postTaskRegisterReqParams.setPostProcessRegistrationParm(registrationParam);
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);


        //-------------------------------------------------------------------------
        //
        //   Post Process
        //
        //-------------------------------------------------------------------------
        // Post-Processing Execution Section
        //-------------------------------------
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(objCommon.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        postTaskExecuteReqParams.setClaimMemo("");
        postController.postTaskExecuteReq(postTaskExecuteReqParams);
        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/bonding_map/rpt")
    @Override
    @CimMapping(TransactionIDEnum.BONDING_MAP_RPT)
    public Response bondingMapRpt(@RequestBody Params.BondingMapResultRptInParams bondingMapResultRptInParams) {
        Validations.check(null == bondingMapResultRptInParams, retCodeConfig.getInvalidParameter());
        String transactionID = TransactionIDEnum.BONDING_MAP_RPT.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, bondingMapResultRptInParams.getUser());

        // Set Event Parameter
        List<Infos.EventParameter> eventParameters = new ArrayList<>();
        eventParameters.add(new Infos.EventParameter("EQP_ID", ObjectIdentifier.fetchValue(bondingMapResultRptInParams.getEquipmentID())));
        eventParameters.add(new Infos.EventParameter("CONTROL_JOB_ID", ObjectIdentifier.fetchValue(bondingMapResultRptInParams.getControlJobID())));
        Optional.ofNullable(bondingMapResultRptInParams.getBondingMapInfoList()).ifPresent(list -> {
            StringBuilder baseWaferID = new StringBuilder();
            StringBuilder topWaferID = new StringBuilder();
            Iterator<Infos.BondingMapInfo> iterator = list.iterator();
            if (iterator.hasNext()) {
                Infos.BondingMapInfo next = iterator.next();
                baseWaferID.append(ObjectIdentifier.fetchValue(next.getBaseWaferID()));
                topWaferID.append(ObjectIdentifier.fetchValue(next.getActualTopWaferID()));
                if (iterator.hasNext()) {
                    baseWaferID.append(",");
                    topWaferID.append(",");
                }
            }
            eventParameters.add(new Infos.EventParameter("BASE_WAFER_ID", baseWaferID.toString()));
            eventParameters.add(new Infos.EventParameter("TOP_WAFER_ID", topWaferID.toString()));
        });
        eventParameters.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));

        //------------------------------
        // bondingMap_FillInTxPCR003DR
        //------------------------------
        Outputs.ObjBondingMapFillInTxPCR003DROut objBondingMapFillInTxPCR003DROut = bondingMapMethod.bondingMapFillInTxPCR003DR(objCommon, bondingMapResultRptInParams.getBondingMapInfoList());

        //------------------------------
        // txPrivilegeCheckReq
        //------------------------------
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams();
        checkInqParams.setEquipmentID(bondingMapResultRptInParams.getEquipmentID());
        checkInqParams.setLotIDLists(objBondingMapFillInTxPCR003DROut.getBondingLotIDSeq());
        accessInqService.sxAccessControlCheckInq(objCommon, checkInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        bondService.sxBondingMapResultRpt(objCommon, bondingMapResultRptInParams);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/wafer_bond/req")
    @Override
    @CimMapping(TransactionIDEnum.WAFER_BOND_REQ)
    public Response waferBondReq(@RequestBody Params.WaferStackingReqInParams waferStackingReqInParams) {
        final String transactionID = TransactionIDEnum.WAFER_BOND_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == waferStackingReqInParams, retCodeConfig.getInvalidParameter());
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, waferStackingReqInParams.getUser());

        //------------------------------
        // bondingGroup_info_GetDR
        //------------------------------
        Outputs.ObjBondingGroupInfoGetDROut bondingGroupInfoGetDROut = bondingGroupMethod.bondingGroupInfoGetDR(objCommon, waferStackingReqInParams.getBondingGroupID(), true);

        //------------------------------
        // txPrivilegeCheckReq
        //------------------------------
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams();
        checkInqParams.setEquipmentID(bondingGroupInfoGetDROut.getBondingGroupInfo().getTargetEquipmentID());
        checkInqParams.setLotIDLists(bondingGroupInfoGetDROut.getBondingLotIDList());
        accessInqService.sxAccessControlCheckInq(objCommon, checkInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        bondService.sxWaferStackingReq(objCommon, waferStackingReqInParams);

        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/wafer_bond_cancel/req")
    @Override
    @CimMapping(TransactionIDEnum.WAFER_BOND_CANCEL_REQ)
    public Response waferBondCancelReq(@RequestBody Params.WaferStackingCancelReqInParams waferStackingCancelReqInParams) {
        final String transactionID = TransactionIDEnum.WAFER_BOND_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == waferStackingCancelReqInParams, retCodeConfig.getInvalidParameter());
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, waferStackingCancelReqInParams.getUser());
        Inputs.ObjLotStackedWaferInfoGetDRIn lotStackedWaferInfoGetDRIn = new Inputs.ObjLotStackedWaferInfoGetDRIn();
        lotStackedWaferInfoGetDRIn.setTopLotIDSeq(waferStackingCancelReqInParams.getTopLotIDSeq());
        Outputs.ObjLotStackedWaferInfoGetDROut lotStackedWaferInfoGetDROut = lotMethod.lotStackedWaferInfoGetDR(objCommon, lotStackedWaferInfoGetDRIn);

        //------------------------------
        // txPrivilegeCheckReq
        //------------------------------
        Params.AccessControlCheckInqParams checkInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDs = new ArrayList<>(waferStackingCancelReqInParams.getTopLotIDSeq());
        if (null != lotStackedWaferInfoGetDROut) {
            lotIDs.addAll(lotStackedWaferInfoGetDROut.getBaseLotIDSeq());
        }
        checkInqParams.setLotIDLists(lotIDs);
        accessInqService.sxAccessControlCheckInq(objCommon, checkInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        bondService.sxWaferStackingCancelReq(objCommon, waferStackingCancelReqInParams);

        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @PostMapping(value = "/bonding_group_partial_remove/req")
    @Override
    @CimMapping(TransactionIDEnum.BONDING_GROUP_PARTIAL_REMOVE_REQ)
    public Response bondingGroupPartialRemoveReq(@RequestBody Params.BondingGroupPartialReleaseReqInParam param) {
        final String transactionID = TransactionIDEnum.BONDING_GROUP_PARTIAL_REMOVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, param.getUser());

        List<Infos.EventParameter> eventParameters = new ArrayList<>();
        List<Infos.BondingGroupReleaseLotWafer> strBondingGroupReleaseLotWaferSeq = param.getStrBondingGroupReleaseLotWaferSeq();
        if (CimArrayUtils.isNotEmpty(strBondingGroupReleaseLotWaferSeq)) {
            strBondingGroupReleaseLotWaferSeq.forEach(lotWafer -> {
                eventParameters.add(new Infos.EventParameter("BOND_GRP_ID", lotWafer.getBondingGroupID()));
                eventParameters.add(new Infos.EventParameter("LOT_ID", ObjectIdentifier.fetchValue(lotWafer.getParentLotID())));
            });
        }
        eventParameters.forEach(event -> log.info(String.format("Name: %s : Value %s", event.getParameterName(), event.getParameterValue())));

        //------------------------------
        // txPrivilegeCheckReq
        //------------------------------
        accessInqService.sxAccessControlCheckInq(objCommon, new Params.AccessControlCheckInqParams());

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Infos.BondingGroupReleasedLot> retVal = bondService.sxBondingGroupPartialReleaseReq(objCommon, param);

        return Response.createSucc(transactionID, retVal);
    }
}
