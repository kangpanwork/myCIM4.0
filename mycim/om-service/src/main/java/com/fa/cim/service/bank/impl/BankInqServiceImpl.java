package com.fa.cim.service.bank.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.method.IBankMethod;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.ISorterNewMethod;
import com.fa.cim.service.bank.IBankInqService;
import com.fa.cim.sorter.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 14:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class BankInqServiceImpl implements IBankInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IBankMethod bankMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    @Autowired
    private ILotMethod lotMethod;

    /**
     * description:
     *
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @since  2018/3/20
     * @param objCommon -
     * @param bankListInqParams - bank List Inq Params
     * @return com.fa.cim.dto.result.RetCode<BankListInqResult>
     */
    public Results.BankListInqResult sxBankListInq(Infos.ObjCommon objCommon, Params.BankListInqParams bankListInqParams) {
        // get all register bank information
        log.debug(" get all register bank information");
        return bankMethod.bankGetByProductionBankDR(objCommon, bankListInqParams);
    }

    /**
     * description: txCarrierDetailInfoInq__170
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @since 11/23/2018 10:53 AM
     * @param objCommon -
     * @param carrierDetailInfoInqParams -
     * @return com.fa.cim.dto.RetCode<Results.CarrierDetailInfoInqResult>
     */
    public Results.CarrierDetailInfoInqResult sxCarrierDetailInfoInq(Infos.ObjCommon objCommon, Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams) {

        //step1 - cassetteDBInfoGetDR;
        Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfo = new Infos.CassetteDBINfoGetDRInfo();
        cassetteDBINfoGetDRInfo.setCassetteID(carrierDetailInfoInqParams.getCassetteID());
        cassetteDBINfoGetDRInfo.setDurableOperationInfoFlag(carrierDetailInfoInqParams.getDurableOperationInfoFlag());
        cassetteDBINfoGetDRInfo.setDurableWipOperationInfoFlag(carrierDetailInfoInqParams.getDurableWipOperationInfoFlag());
        Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDRRetCode = cassetteMethod.cassetteDBInfoGetDR(objCommon, cassetteDBINfoGetDRInfo);

        com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn cassetteInPostProcessFlagGetIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
        cassetteInPostProcessFlagGetIn.setCarrierID(carrierDetailInfoInqParams.getCassetteID());
        List<Info.SortJobListAttributes> objSorterJobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon, cassetteInPostProcessFlagGetIn);
        Infos.CassetteStatusInfo cassetteStatusInfo = cassetteDBInfoGetDRRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo();
        if (!CimArrayUtils.isEmpty(objSorterJobListGetDROut)) {
            cassetteStatusInfo.setSorterJobExistFlag(true);
        } else {
            cassetteStatusInfo.setSorterJobExistFlag(false);
        }
        //step 2 Get InPostProcessFlag of cassette
        Boolean objCassetteInPostProcessFlagGetOut = cassetteMethod.cassetteInPostProcessFlagGet(objCommon, carrierDetailInfoInqParams.getCassetteID());
        cassetteStatusInfo.setInPostProcessFlagOfCassettea(objCassetteInPostProcessFlagGetOut);

        return cassetteDBInfoGetDRRetCode.getCarrierDetailInfoInqResult();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param carrierListInqParams -
     * @param objCommon            -
     * @return com.fa.cim.dto.result.CarrierListInqResult
     * @author Bear
     * @since 2018/5/25
     */
    public Results.CarrierListInq170Result sxCarrierListInq(Infos.ObjCommon objCommon, Params.CarrierListInqParams carrierListInqParams) {
        Inputs.ObjCassetteListGetDRIn cassetteListGetDRIn = new Inputs.ObjCassetteListGetDRIn(carrierListInqParams);
        //【step1】call cassetteListGetDR
        log.debug("【step1】call cassetteListGetDR");
        //return cassetteMethod.cassetteListGetDR(objCommon, cassetteListGetDRIn);
        Results.CarrierListInq170Result carrierListInq170Result = new Results.CarrierListInq170Result();
        carrierListInq170Result.setFoundCassette(cassetteMethod.cassetteListGetDR170(objCommon, cassetteListGetDRIn));
        return carrierListInq170Result;
    }

    @Override
    public Results.MaterialPrepareCancelInfoInqResult sxMaterialPrepareCancelInfoInq(Infos.ObjCommon objCommon, Params.MaterialPrepareCancelInfoInqParams materialPrepareCancelInfoInqParams) {

        Results.MaterialPrepareCancelInfoInqResult out = new Results.MaterialPrepareCancelInfoInqResult();
        //------------------------------------------------------
        //  Check input preparationCancelledLotID
        //------------------------------------------------------
        if (null == materialPrepareCancelInfoInqParams.getPreparationCancelledLotID()) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        //------------------------------------------------------
        //  Check SP_LOT_PREPARECANCEL
        //------------------------------------------------------
        /*
        String preparationCancelEnv = environmentVariableManager.getValue(EnvConst.SP_LOT_PREPARECANCEL);
        if (!StringUtils.equals(BizConstant.SP_LOT_PREPARECANCEL_ON, preparationCancelEnv)) {
            throw new ServiceException(retCodeConfig.getLotPrepareCancelOff());
        }
        */
        //------------------------------------------------------
        //  Get lot Preparation Cancel Information
        //------------------------------------------------------
        Outputs.ObjLotPreparationCancelInfoGetDROut lotPreparationCancelInfoOut = lotMethod.lotPreparationCancelInfoGetDR(objCommon, materialPrepareCancelInfoInqParams.getPreparationCancelledLotID());

        out.setNewVendorLotInfoList(lotPreparationCancelInfoOut.getNewVendorLotInfoList());
        out.setPreparationCancelledLotInfo(lotPreparationCancelInfoOut.getPreparationCancelledLotInfo());
        out.setPreparationCancelledWaferInfoList(lotPreparationCancelInfoOut.getPreparationCancelledWaferInfoList());
        return out;
    }

}
