package com.fa.cim.controller.bond;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.bond.IBondInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.bond.IBondInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>BondingInqController .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/16 10:24         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/16 10:24
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@Listenable
@RestController
@RequestMapping("/bond")
public class BondInqController implements IBondInqController {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IBondInqService bondInqService;

    @ResponseBody
    @PostMapping(value = "/bonding_group_list/inq")
    @Override
    @CimMapping(TransactionIDEnum.BONDING_GROUP_LIST_INQ)
    public Response bondingGroupListInq(@RequestBody Params.BondingGroupListInqInParams bondingGroupListInqInParams) {
        final String transactionID = TransactionIDEnum.BONDING_GROUP_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == bondingGroupListInqInParams, retCodeConfig.getInvalidParameter());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, bondingGroupListInqInParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Infos.BondingGroupInfo> bondingGroupInfos = bondInqService.sxBondingGroupListInq(objCommon, bondingGroupListInqInParams);

        return Response.createSucc(transactionID, bondingGroupInfos);
    }

    @ResponseBody
    @PostMapping(value = "/bonded_wafer_list/inq")
    @Override
    @CimMapping(TransactionIDEnum.BONDED_WAFER_LIST_INQ)
    public Response bondedWaferListInq(@RequestBody Params.StackedWaferListInqInParams stackedWaferListInqInParams) {
        final String transactionID = TransactionIDEnum.BONDED_WAFER_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == stackedWaferListInqInParams, retCodeConfig.getInvalidParameter());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(stackedWaferListInqInParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, stackedWaferListInqInParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Infos.StackedWaferInfo> stackedWaferInfos = bondInqService.sxStackedWaferListInq(objCommon, stackedWaferListInqInParams);

        return Response.createSucc(transactionID, stackedWaferInfos);
    }

    @ResponseBody
    @PostMapping(value = "/bonding_flow_list/inq")
    @Override
    @CimMapping(TransactionIDEnum.BONDING_FLOW_LIST_INQ)
    public Response bondingFLowListInq(@RequestBody Params.BondingFLowListInqInParams bondingFLowListInqInParams) {
        final String transactionID = TransactionIDEnum.BONDING_FLOW_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == bondingFLowListInqInParams, retCodeConfig.getInvalidParameter());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, bondingFLowListInqInParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<String> retVal = bondInqService.sxBondingFlowSectionListInq(objCommon);

        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/lot_list_by_bonding_flow/inq")
    @Override
    @CimMapping(TransactionIDEnum.LOT_LIST_BY_BONDING_FLOW_INQ)
    public Response lotListByBondingFlowInq(@RequestBody Params.LotListInBondingFlowInqInParams lotListInBondingFlowInqInParams) {
        final String transactionID = TransactionIDEnum.LOT_LIST_BY_BONDING_FLOW_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == lotListInBondingFlowInqInParams, retCodeConfig.getInvalidParameter());
        ObjectIdentifier equipmentID = null;
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.HashedInfo hashedInfo : lotListInBondingFlowInqInParams.getStrSearchConditionSeq()) {
            if (CimStringUtils.equals(hashedInfo.getHashKey(), BizConstant.SP_HASHKEY_TARGETEQUIPMENTID)) {
                equipmentID = ObjectIdentifier.buildWithValue(hashedInfo.getHashData());
            }
            if (CimStringUtils.equals(hashedInfo.getHashKey(), BizConstant.SP_HASHKEY_LOTID)) {
                lotIDs.add(ObjectIdentifier.buildWithValue(hashedInfo.getHashData()));
            }
        }
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(equipmentID);
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, lotListInBondingFlowInqInParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Infos.LotInBondingFlowInfo> retVal = bondInqService.sxLotListInBondingFlowInq(objCommon, lotListInBondingFlowInqInParams);

        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/bonding_lot_list/inq")
    @Override
    @CimMapping(TransactionIDEnum.BONDING_LOT_LIST_IINQ)
    public Response bondingLotListInq(@RequestBody Params.BondingLotListInqInParams bondingLotListInqInParams) {
        final String transactionID = TransactionIDEnum.BONDING_LOT_LIST_IINQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == bondingLotListInqInParams, retCodeConfig.getInvalidParameter());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(bondingLotListInqInParams.getTargetEquipmentID());
        accessControlCheckInqParams.setProductIDList(Collections.singletonList(bondingLotListInqInParams.getProductID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, bondingLotListInqInParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Infos.BondingLotAttributes> retVal = bondInqService.sxBondingLotListInq(objCommon, bondingLotListInqInParams);

        return Response.createSucc(transactionID, retVal);
    }

    @ResponseBody
    @PostMapping(value = "/eqp_bonding_candidate/inq")
    @Override
    @CimMapping(TransactionIDEnum.EQP_BONDING_CANDIDATE_INQ)
    public Response eqpBondingCandidateInq(@RequestBody Params.EqpCandidateForBondingInqInParams eqpCandidateForBondingInqInParams) {
        final String transactionID = TransactionIDEnum.EQP_BONDING_CANDIDATE_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == eqpCandidateForBondingInqInParams, retCodeConfig.getInvalidInputParam());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, eqpCandidateForBondingInqInParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Infos.AreaEqp> retVal = bondInqService.sxEqpCandidateForBondingInq(objCommon, eqpCandidateForBondingInqInParams);
        return Response.createSucc(transactionID, retVal);
    }
}
