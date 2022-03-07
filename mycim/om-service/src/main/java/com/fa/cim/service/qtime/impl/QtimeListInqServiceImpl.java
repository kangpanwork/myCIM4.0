package com.fa.cim.service.qtime.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IQTimeMethod;
import com.fa.cim.service.qtime.IQtimeListInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/7        ********             Jerry               create file
 *
 * @author: Jerry
 * @date: 2018/11/7 14:44
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@OmService
@Slf4j
public class QtimeListInqServiceImpl implements IQtimeListInqService {

    @Autowired
    private IQTimeMethod qtimeMethod;

    @Autowired
    private ILotMethod lotMethod;

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2018/11/13 9:30
     * @param objCommon
     * @param qtimeListInqInfo -
     * @return com.fa.cim.dto.RetCode<Results.QtimeListInqResult>
     */
    @Override
    public List<Outputs.QrestLotInfo> sxQtimeListInq(Infos.ObjCommon objCommon, Infos.QtimeListInqInfo qtimeListInqInfo) {
        List<Outputs.QrestLotInfo> out = null;
        if (!ObjectIdentifier.isEmptyWithValue(qtimeListInqInfo.getLotID()) || !ObjectIdentifier.isEmptyWithValue(qtimeListInqInfo.getWaferID())) {
            //--------------------------------------------------------
            // Get Q-Time restriction action information for one lot
            //--------------------------------------------------------
            List<Outputs.QrestLotInfo> qrestLotInfoRetCode = qtimeMethod.qTimeLotInfoGetDR(objCommon, qtimeListInqInfo);
            out = qrestLotInfoRetCode;
        } else {
            //---------------------------------------------------
            // Get Q-Time restriction information for every lot
            //---------------------------------------------------
            List<Outputs.QrestLotInfo> qTimeLotListGetDR = qtimeMethod.qTimeLotListGetDR(objCommon, qtimeListInqInfo.getQTimeType());
            out = qTimeLotListGetDR;
        }
        //---------------------------------------------------
        // Get lot information for all lots
        //---------------------------------------------------
        Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
        lotInfoInqFlag.setLotBasicInfoFlag(true);
        lotInfoInqFlag.setLotControlUseInfoFlag(false);
        lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
        lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
        lotInfoInqFlag.setLotOperationInfoFlag(true);
        lotInfoInqFlag.setLotOrderInfoFlag(false);
        lotInfoInqFlag.setLotControlJobInfoFlag(false);
        lotInfoInqFlag.setLotProductInfoFlag(false);
        lotInfoInqFlag.setLotRecipeInfoFlag(false);
        lotInfoInqFlag.setLotLocationInfoFlag(true);
        lotInfoInqFlag.setLotWipOperationInfoFlag(false);
        lotInfoInqFlag.setLotBackupInfoFlag(false);
        List<Outputs.QrestLotInfo> qrestLotInfos = new ArrayList<>();
        for (int i = 0; i < out.size(); i++) {
            Infos.LotInfo objLotDetailInfoGetDROut = lotMethod.lotDBInfoGetDR(objCommon, lotInfoInqFlag, out.get(i).getLotID());

            Outputs.QrestLotInfo qrestLotInfo = out.get(i);
            qrestLotInfo.setCassetteID(objLotDetailInfoGetDROut.getLotLocationInfo().getCassetteID());
            qrestLotInfo.setLotStatus(objLotDetailInfoGetDROut.getLotBasicInfo().getLotStatus());
            qrestLotInfo.setStockerID(objLotDetailInfoGetDROut.getLotLocationInfo().getStockerID());
            qrestLotInfo.setTransferStatus(objLotDetailInfoGetDROut.getLotLocationInfo().getTransferStatus());
            qrestLotInfo.setRouteID(objLotDetailInfoGetDROut.getLotOperationInfo().getRouteID());
            qrestLotInfo.setOperationID(objLotDetailInfoGetDROut.getLotOperationInfo().getOperationID());
            qrestLotInfo.setOperationNumber(objLotDetailInfoGetDROut.getLotOperationInfo().getOperationNumber());
            qrestLotInfo.setEquipmentID(objLotDetailInfoGetDROut.getLotLocationInfo().getEquipmentID());
        }
        return out;
    }

}