package com.fa.cim.service.bank;


import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 13:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IBankInqService {

    /**
     * description: Get bank list from PROD_BANK and BANK_ID
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/3/17 10:20                     ZH                Create
     *
     * @author ZH
     * @date 2021/3/17 10:20
     * @param objCommon - User permission information
     * @param bankListInqParams - Query bank input parameter information
     * @return com.fa.cim.dto.Results.BankListInqResult
     */
    Results.BankListInqResult sxBankListInq(Infos.ObjCommon objCommon, Params.BankListInqParams bankListInqParams);

    Results.CarrierDetailInfoInqResult sxCarrierDetailInfoInq(Infos.ObjCommon objCommon, Params.CarrierDetailInfoInqParams carrierDetailInfoInqParams);

    Results.CarrierListInq170Result sxCarrierListInq(Infos.ObjCommon objCommon, Params.CarrierListInqParams carrierListInqParams);

    Results.MaterialPrepareCancelInfoInqResult sxMaterialPrepareCancelInfoInq(Infos.ObjCommon objCommon, Params.MaterialPrepareCancelInfoInqParams materialPrepareCancelInfoInqParams);

}
