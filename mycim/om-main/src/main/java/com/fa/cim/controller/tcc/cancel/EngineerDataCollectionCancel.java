package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.engineerDataCollection.IEngineerDataCollectionController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 14:08
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("DataCollectionCancel")
@Transactional(rollbackFor = Exception.class)
public class EngineerDataCollectionCancel implements IEngineerDataCollectionController {
    @Override
    public Response edcWithSpecCheckActionReq(Params.EDCWithSpecCheckActionReqParams params) {
        return null;
    }

    @Override
    public Response specCheckReq(Params.SpecCheckReqParams params) {
        return null;
    }

    @Override
    public Response edcTransitDataRpt(Params.EDCTransitDataRptParams params) {
        return null;
    }

    @Override
    public Response spcCheckReq(Params.SPCCheckReqParams spcCheckReqParams) {
        return null;
    }

    @Override
    public Response spcDoActionReq(Params.SPCDoActionReqParams spcDoActionReqParams) {
        return null;
    }

    @Override
    public Response edcWithSpecCheckActionByPJReq(Params.EDCWithSpecCheckActionByPJReqParams edcWithSpecCheckActionByPJReqParams) {
        return null;
    }

    @Override
    public Response edcByPJRpt(Params.EDCByPJRptInParms params) {
        return null;
    }

    @Override
    public Response dchubDataSendCompleteRpt(Params.DChubDataSendCompleteRptInParam strDChubDataSendCompleteRptInParam) {
        return null;
    }

    @Override
    public Response edcDataUpdateForLotReq(Params.EDCDataUpdateForLotReqInParm strEDCDataUpdateForLotReqInParm) {
        return null;
    }

    @Override
    public Response edcWithSpecCheckActionByPostTaskReq(Params.EDCWithSpecCheckActionByPostTaskReqParams params) {
        return null;
    }
}