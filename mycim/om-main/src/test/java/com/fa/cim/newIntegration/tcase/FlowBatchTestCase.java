package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.controller.flowbatch.FlowBatchController;
import com.fa.cim.controller.flowbatch.FlowBatchInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/13       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/11/13 15:06
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class FlowBatchTestCase {

    @Autowired
    private FlowBatchInqController flowBatchInqController;

    @Autowired
    private FlowBatchController flowBatchController;

    public User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        return user;
    }



    public Results.FlowBatchLotSelectionInqResult getCandidateLotsList(Params.FlowBatchLotSelectionInqParam params){
        params.setUser(getUser());
        return (Results.FlowBatchLotSelectionInqResult) flowBatchInqController.flowBatchLotSelectionInq(params).getBody();
    }

    public Results.FlowBatchInfoInqResult getFlowBatchInfo(Params.FlowBatchInfoInqParams params){
        params.setUser(getUser());
        return (Results.FlowBatchInfoInqResult) flowBatchInqController.flowBatchInfoInq(params).getBody();
    }

    public Results.FloatingBatchListInqResult getFloatingBatchInfo(ObjectIdentifier equipmentID){
        Params.FloatingBatchListInqParams params = new Params.FloatingBatchListInqParams();
        params.setUser(getUser());
        params.setEquipmentID(equipmentID);
        return (Results.FloatingBatchListInqResult) flowBatchInqController.floatingBatchListInq(params).getBody();
    }

    public Response flowBatchByManualActionReq (Params.FlowBatchByManualActionReqParam param){
        param.setUser(getUser());
        return flowBatchController.flowBatchByManualActionReq(param);
    }

    public Response eqpReserveCancelForflowBatchReq(Params.EqpReserveCancelForflowBatchReqParams params){
        params.setUser(getUser());
        return flowBatchController.eqpReserveCancelForflowBatchReq(params);
    }

    public Response eqpReserveForFlowBatchReq(Params.EqpReserveForFlowBatchReqParam params){
        params.setUser(getUser());
        return flowBatchController.eqpReserveForFlowBatchReq(params);
    }

    public Results.ReFlowBatchByManualActionReqResult reFlowBatchByManualActionReq(Params.ReFlowBatchByManualActionReqParam params){
        params.setUser(getUser());
        return (Results.ReFlowBatchByManualActionReqResult) flowBatchController.reFlowBatchByManualActionReq(params).getBody();
    }

    public Response flowBatchLotRemoveReq(Params.FlowBatchLotRemoveReq params){
        params.setUser(getUser());
        return flowBatchController.flowBatchLotRemoveReq(params);
    }

    public Response autoFlowBatchByManualActionReq(Params.FlowBatchByAutoActionReqParams params){
        params.setUser(getUser());
        return flowBatchController.autoFlowBatchByManualActionReq(params);
    }

    public Response eqpMaxFlowbCountModifyReq(Params.EqpMaxFlowbCountModifyReqParams params){
        params.setUser(getUser());
        return flowBatchController.eqpMaxFlowbCountModifyReq(params);
    }

    public Response flowBatchCheckForLotSkipReq(Params.FlowBatchCheckForLotSkipReqParams params){
        params.setUser(getUser());
        return flowBatchController.flowBatchCheckForLotSkipReq(params);
    }
    public List<Infos.FlowBatchLostLotInfo> getflowBatchLostLots(){
        Params.FlowBatchStrayLotsListInqParams flowBatchStrayLotsListInqParams = new Params.FlowBatchStrayLotsListInqParams();
        flowBatchStrayLotsListInqParams.setUser(getUser());
        return ((Results.FlowBatchStrayLotsListInqResult) flowBatchInqController.flowBatchStrayLotsListInq(flowBatchStrayLotsListInqParams).getBody()).getFlowBatchLostLotsList();
    }

}