package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;

import java.util.List;

/**
 * description:
 * IVirtualOperationMethod .
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/8/10        ********             PlayBoy               create file
 * 2019/9/23        ######              Neko                Refactor: change retCode to exception
 *
 * @author PlayBoy
 * @since 2018/8/10 11:13
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IVirtualOperationMethod {

    /**
     * description:
     * Check virtual operation by start cassette.
     * change history:
     * date             defect             person             comments
     * --------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @return RetCode
     * @author PlayBoy
     * @date 2018/8/10
     */
    Boolean virtualOperationCheckByStartCassette(Infos.ObjCommon objCommon, List<Infos.StartCassette>
            startCassetteList);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/3/13 11:22
     * @param objCommon -
     * @param in -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.bean.extension.Infos.VirtualOperationLot>>
     */
    List<Infos.VirtualOperationLot> virtualOperationLotsGetDR(Infos.ObjCommon objCommon, Inputs.ObjVirtualOperationLotsGetDRIn in);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2019/3/13 11:32
     * @param objCommon -
     * @param in -
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.bean.extension.Infos.VirtualOperationInprocessingLot>>
     */
    List<Infos.VirtualOperationInprocessingLot> virtualOperationInprocessingLotsGetDR(Infos.ObjCommon objCommon, Inputs.ObjVirtualOperationInprocessingLotsGetDRIn in);

}
